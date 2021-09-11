package org.wvt.horizonmgr.utils

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import org.wvt.horizonmgr.webapi.pack.OfficialCDNPackage
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

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

    fun download(pack: OfficialCDNPackage) =
        object : org.wvt.horizonmgr.service.ProgressDeferred<Float, DownloadResult> {
            private val scope = CoroutineScope(EmptyCoroutineContext + Dispatchers.IO)
            private val channel = Channel<Float>(Channel.UNLIMITED)
            private val job = scope.async {
                val zipFile = downloadPacksDir.resolve("${pack.uuid}.zip")
                val graphicsFile = downloadDir.resolve("${pack.uuid}_graphics.zip")

                channel.send(0f)

                // 这个比较小就不算进度了吧
                val graphicsJob = async {
                    graphicsFile.outputStream().use {
                        CoroutineDownloader.download(pack.graphicsUrl, it).await()
                    }
                }

                val packJob = async {
                    // TODO: Use multi-thread download
                    zipFile.outputStream().use { stream ->
                        // 遍历区块
                        pack.chunks.sortedBy { it.index }.forEach { chunk ->
                            val task = CoroutineDownloader.download(chunk.url, stream)
                            task.progressChannel().receiveAsFlow().conflate().collect {
                                channel.send((chunk.index + it) / pack.chunks.size)
                            }
                            task.await() // 等待该区块下载完成后再下载其他区块
                        }
                    }
                }

                graphicsJob.await()
                packJob.await()
                channel.close()
                return@async DownloadResult(zipFile, graphicsFile)
            }

            override suspend fun await(): DownloadResult = job.await()

            override suspend fun progressChannel(): ReceiveChannel<Float> = channel
        }

    fun download2(pack: OfficialCDNPackage): PackageDownloadTask {
        return PackageDownloadTask(pack, downloadPacksDir, downloadDir)
    }
}

/**
 * 解析在线分包数据
 * 创建区块文件
 * 下载区块文件，汇报进度
 * 合并区块文件
 * 完成
 */
class PackageDownloadTask internal constructor(
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
            val task = CoroutineDownloader.download(pack.graphicsUrl, it)
            // TODO: 2021/6/11  
        }
    }

    private suspend fun chunkTask() = coroutineScope {
        pack.chunks.forEach { chunk ->
            launch {
                val chunkFile = File(zipFile.absolutePath + chunk.index)
                chunkFile.outputStream().use { stream ->
                    val task = CoroutineDownloader.download(chunk.url, stream)
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
