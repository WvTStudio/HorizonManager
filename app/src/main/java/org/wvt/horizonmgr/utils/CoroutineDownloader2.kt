package org.wvt.horizonmgr.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 下载文件有如下状态
 * 1. 解析 URL
 * 2. 获取文件信息：如大小
 * 3. 下载
 * 4. 多线程下载
 */
object CoroutineDownloader2 {
    interface DownloadTask {
        val downloadState: MutableStateFlow<DownloadState>
        suspend fun await()
        suspend fun cancel()
    }

    fun download(url: String, output: OutputStream): DownloadTask {
        return DownloadTaskImpl(url, output)
    }

    sealed class DownloadState {
        object Parsing : DownloadState()

        data class Downloading(
            val size: Long,
            val downloaded: StateFlow<Long>
        ) : DownloadState()

        data class Error(val e: Throwable) : DownloadState()

        data class Succeed(val bytesDownloaded: Long) : DownloadState()
    }

    private class DownloadTaskImpl(url: String, output: OutputStream) : DownloadTask {
        private val scope = CoroutineScope(Dispatchers.IO)

        override val downloadState = MutableStateFlow<DownloadState>(DownloadState.Parsing)

        private val job = scope.async {
            downloadState.emit(DownloadState.Parsing)
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.doOutput = false
                conn.connect()

                val contentLength = conn.getHeaderField("Content-Length").toLongOrNull() ?: -1

                conn.inputStream.use { input ->
                    val downloadedBytes = MutableStateFlow(0L)

                    downloadState.emit(
                        DownloadState.Downloading(
                            contentLength,
                            downloadedBytes.asStateFlow()
                        )
                    )

                    var bytesCopied: Long = 0
                    val buffer = ByteArray(8 * 1024)
                    var bytes = input.read(buffer)

                    while (bytes >= 0 && isActive) {
                        output.write(buffer, 0, bytes)
                        bytesCopied += bytes
                        downloadedBytes.emit(bytesCopied)
                        bytes = input.read(buffer)
                    }

                    conn.disconnect()
                    downloadState.emit(DownloadState.Succeed(bytesCopied))
                    return@async
                }
            } catch (e: Exception) {
                downloadState.emit(DownloadState.Error(e))
                return@async
            }
        }

        override suspend fun await() {
            return job.await()
        }

        override suspend fun cancel() {
            job.cancel()
        }
    }
}