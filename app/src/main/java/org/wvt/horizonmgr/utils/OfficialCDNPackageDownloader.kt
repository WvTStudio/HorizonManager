package org.wvt.horizonmgr.utils

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.wvt.horizonmgr.webapi.pack.OfficialCDNPackage
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

private const val TAG = "OfficialCDNPackageDownl"

/**
 * 下载一个 OfficialCDN Package 需要以下步骤：
 * 要求每一步都可以回显
 * 解析：
 *  知道分包的 UUID
 *  获取分包文件的 Chunk
 * 下载：
 *  下载每一个 Chunk
 *  合并 Chunk 为一个文件
 * 安装
 */
class OfficialCDNPackageDownloader(context: Context) {
    private val downloadDir = context.filesDir.resolve("downloads")
        get() = field.also { if (!it.exists()) it.mkdirs() }

    private val downloadPacksDir = downloadDir.resolve("packs")
        get() = field.also { if (!it.exists()) it.mkdirs() }

    data class DownloadResult(
        val packageZipFile: File,
        val graphicsFile: File
    )

    fun download(pack: OfficialCDNPackage): DownloadTask {
        return DownloadTask(pack)
    }

    inner class DownloadTask(private val pack: OfficialCDNPackage) {
        private val scope = CoroutineScope(EmptyCoroutineContext + Dispatchers.IO)

        // Chunk index, progress
        private val _progress = MutableStateFlow<Pair<Long, Long>>(0L to -1L)
        val progress = _progress.asStateFlow()

        private lateinit var job: Deferred<DownloadResult>

        private val zipFile = downloadPacksDir.resolve("${pack.uuid}.zip")
        private val graphicsFile = downloadDir.resolve("${pack.uuid}_graphics.zip")

        init {
            start()
        }

        private fun start() {
            job = scope.async {
                coroutineScope {
                    // Graphics
                    launch {
                        downloadGraphic()
                    }

                    launch {
                        val files = downloadChunks()
                        // Merge
                        mergeFiles(files)
                    }
                }
                return@async DownloadResult(zipFile, graphicsFile)
            }
        }

        private suspend fun downloadGraphic() {
            val task = FileDownloader.newTask(pack.graphicsUrl)
            graphicsFile.outputStream().use {
                task.setOutput(it)
                task.connect()
                task.start()
                task.await()
            }
        }

        private suspend fun downloadChunks(): List<File> {
            val files = pack.chunks.map { File(zipFile.absolutePath + ".part_${it.index}") }
            var total: Long = 0L

            // Download to files
            val tasks = pack.chunks.map { chunk ->
                val task = FileDownloader.newTask(chunk.url)
                val size = task.connect()
                total += size
                task
            }

            val mutex = Mutex()
            val downloaded = LongArray(tasks.size) { 0L }

            coroutineScope {
                tasks.forEachIndexed { index, task ->
                    launch {
                        val file = files[index]
                        val output = file.outputStream().buffered()
                        task.setOutput(output)
                        val state = task.start()
                        val job = launch {
                            state.collect {
                                mutex.withLock {
                                    downloaded[index] = it
                                    _progress.emit(downloaded.sum() to total)
                                }
                                delay(500)
                            }
                        }
                        try {
                            task.await()
                        } finally {
                            job.cancel()
                            output.close()
                        }
                    }
                }
            }

            return files
        }

        private suspend fun mergeFiles(files: List<File>) = withContext(Dispatchers.IO) {
            zipFile.outputStream().use { output ->
                files.forEach {
                    output.write(it.readBytes())
                    it.delete()
                }
            }
        }

        suspend fun await(): DownloadResult = job.await()
    }
}

/**
 * 解析在线分包数据
 * 创建区块文件
 * 下载区块文件，汇报进度
 * 合并区块文件
 * 完成
 * TODO: Should I complete this?
 */
@Deprecated("Not implemented")
private class PackageDownloadTask internal constructor(
    private val pack: OfficialCDNPackage,
    private val downloadPacksDir: File,
    private val downloadDir: File
) {
    private val zipFile = downloadPacksDir.resolve("${pack.uuid}.zip")
    private val graphicsFile = downloadPacksDir.resolve("${pack.uuid}_graphics.zip")

    sealed class DownloadState {
        object Parsing : DownloadState()

        data class Downloading(
            val size: Long,
            val downloaded: StateFlow<Long>
        ) : DownloadState()

        data class Error(val e: Throwable) : DownloadState()
    }

    sealed class ChunkDownloadState {
        object Parsing : ChunkDownloadState()
        data class Downloading(
            val chunks: List<MutableStateFlow<DownloadState>>
        ) : ChunkDownloadState()

        data class Error(val e: Throwable) : ChunkDownloadState()
    }

    val graphicsDownloadState = MutableStateFlow<DownloadState>(DownloadState.Parsing)
    val chunkDownloadState = MutableStateFlow<ChunkDownloadState>(ChunkDownloadState.Parsing)

    private val chunkFiles = mutableListOf<File>()

    private suspend fun start() = coroutineScope {
        coroutineScope {
            // Graphics Task
            launch { graphicsTask() }
            // Chunk Tasks
            launch { chunkTask() }
        }
        // Merge
        mergeTask()
        // Delete Chunk Files
        chunkFiles.forEach { it.delete() }
        // Finish
    }

    fun cancel() {
        TODO()
    }

    suspend fun cancelAndJoin() {
        TODO()
    }

    private suspend fun graphicsTask() {
        graphicsDownloadState.emit(DownloadState.Parsing)

        val downloaded = MutableStateFlow(0L)
        val state = DownloadState.Downloading(0, downloaded.asStateFlow())

        graphicsFile.outputStream().use {
            // TODO: 2021/6/11
        }
    }

    private suspend fun chunkTask() = coroutineScope {
        pack.chunks.forEach { chunk ->
            launch {
                val chunkFile = File(zipFile.absolutePath + chunk.index)
                chunkFile.outputStream().use { stream ->
                    // TODO: 2021/6/11
                }
            }
        }
    }

    private suspend fun mergeTask() {
        zipFile.outputStream().use { main ->
            chunkFiles.forEach { chunk ->
                withContext(Dispatchers.IO) {
                    chunk.inputStream().use {
                        it.copyTo(main)
                    }
                }
            }
        }
    }

    fun retryGraphics() {
// TODO: 2021/6/11  
    }

    fun retryChunk(index: Int) {
// TODO: 2021/6/11  
    }
}
