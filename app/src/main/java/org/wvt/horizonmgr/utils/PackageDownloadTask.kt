package org.wvt.horizonmgr.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.wvt.horizonmgr.webapi.pack.OfficialCDNPackage
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 解析在线分包数据
 * 创建区块文件
 * 下载区块文件，汇报进度
 * 合并区块文件
 * 完成
 * TODO: Should I complete this?
 */
class PackageDownloadTask constructor(
    private val pack: OfficialCDNPackage,
    targetDirectory: File
) {
    private val zipFile = targetDirectory.resolve("${pack.uuid}.zip")
    private val graphicsFile = targetDirectory.resolve("${pack.uuid}_graphics.zip")

    sealed class DownloadState {
        object Idle : DownloadState()
        object Parsing : DownloadState()

        data class Downloading(
            val size: Long,
            val downloaded: StateFlow<Long>
        ) : DownloadState()

        data class Error(val e: Throwable) : DownloadState()
        data class Succeed(val downloaded: Long) : DownloadState()
    }

    sealed class ChunkDownloadState {
        object Idle : ChunkDownloadState()
        object Parsing : ChunkDownloadState()

        data class Downloading(
            val totalSize: Long,
            val downloaded: StateFlow<Long>,
            val chunks: List<StateFlow<DownloadState>>
        ) : ChunkDownloadState()

        data class Succeed(val downloaded: Long) : ChunkDownloadState()
        data class Error(val e: Throwable) : ChunkDownloadState()
    }

    sealed class MergingState {
        object Idle : MergingState()
        data class Merging(val progress: StateFlow<Pair<Int, Int>>) : MergingState()
        data class Error(val e: Throwable) : MergingState()
        object Succeed : MergingState()
    }

    val graphicsDownloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val chunkDownloadState = MutableStateFlow<ChunkDownloadState>(ChunkDownloadState.Idle)
    val mergeState = MutableStateFlow<MergingState>(MergingState.Idle)

    private val chunkFiles = pack.chunks.map {
        File(zipFile.absolutePath + ".part_$it")
    }

    data class Result(
        val graphicsFile: File,
        val packageZipFile: File
    )

    suspend fun run(): Result = coroutineScope {
        coroutineScope {
            // Graphics Task
            launch { graphicsTask() }
            // Chunk Tasks
            launch { chunkTask() }
        }
        // Merge
        mergeTask()
        // Finish
        return@coroutineScope Result(graphicsFile, zipFile)
    }

    private suspend fun graphicsTask() {
        graphicsDownloadState.emit(DownloadState.Parsing)

        val task = FileDownloader.newTask(pack.graphicsUrl)
        val size = task.connect()
        val downloaded = MutableStateFlow(0L)
        val output = graphicsFile.outputStream()
        task.setOutput(output)

        graphicsDownloadState.emit(DownloadState.Downloading(size, downloaded))

        coroutineScope {
            val job = launch {
                task.progress.collect {
                    downloaded.emit(it)
                }
            }
            try {
                task.download()
            } catch (e: Exception) {
                graphicsDownloadState.emit(DownloadState.Error(e))
                return@coroutineScope
            } finally {
                job.cancelAndJoin()
                output.close()
            }
            graphicsDownloadState.emit(DownloadState.Succeed(downloaded.value))
        }
    }

    private suspend fun chunkTask(): Unit = coroutineScope {
        chunkDownloadState.emit(ChunkDownloadState.Parsing)

        // Parse tasks
        val tasks = pack.chunks.map { chunk ->
            async {
                FileDownloader.newTask(chunk.url).also {
                    it.connect()
                }
            }
        }.awaitAll()

        val chunks = tasks.map { task ->
            MutableStateFlow<DownloadState>(
                DownloadState.Downloading(task.getSize(), task.progress)
            )
        }
        val totalSize = tasks.sumOf { it.getSize() }
        val totalDownloaded = MutableStateFlow(0L)

        chunkDownloadState.emit(
            ChunkDownloadState.Downloading(
                totalSize,
                totalDownloaded,
                chunks
            )
        )

        chunks.map { chunk ->
            chunk.flatMapLatest { state ->
                when (state) {
                    is DownloadState.Downloading -> {
                        state.downloaded
                    }
                    is DownloadState.Succeed -> {
                        MutableStateFlow(state.downloaded)
                    }
                    else -> {
                        MutableStateFlow(0L)
                    }
                }
            }
        }.let {
            launch {
                it.reduceRight { flow, acc ->
                    flow.combine(acc) { a, b ->
                        a + b
                    }
                }.onEach {
                    totalDownloaded.emit(it)
                }.collect()
            }
        }

        tasks.mapIndexed { index, task ->
            launch(Dispatchers.IO) {
                val task = task
                val file = chunkFiles[index]

                file.outputStream().use {
                    task.setOutput(it)
                    task.download()
                }
            }
        }.joinAll()

        chunkDownloadState.emit(ChunkDownloadState.Succeed(totalSize))
    }

    private suspend fun mergeTask() {
        val progress = MutableStateFlow(0 to chunkFiles.size)
        mergeState.emit(MergingState.Merging(progress))
        try {
            zipFile.outputStream().use { main ->
                chunkFiles.forEach { chunk ->
                    suspendCoroutine {
                        chunk.inputStream().use {
                            it.copyTo(main)
                        }
                        it.resume(Unit)
                    }
                }
            }
            // Delete Chunk Files
            chunkFiles.forEach { it.delete() }
        } catch (e: Exception) {
            e.printStackTrace()
            mergeState.emit(MergingState.Error(e))
            return
        }
        mergeState.emit(MergingState.Succeed)
    }

    fun retryGraphics() {
// TODO: 2021/6/11
    }

    fun retryChunk(index: Int) {
// TODO: 2021/6/11
    }
}