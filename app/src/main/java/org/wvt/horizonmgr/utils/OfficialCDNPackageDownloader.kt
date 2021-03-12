package org.wvt.horizonmgr.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.receiveAsFlow
import org.wvt.horizonmgr.webapi.pack.OfficialCDNPackage
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

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

}