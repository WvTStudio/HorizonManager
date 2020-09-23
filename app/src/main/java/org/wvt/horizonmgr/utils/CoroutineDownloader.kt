package org.wvt.horizonmgr.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object CoroutineDownloader {
    interface DownloadTask {
        /**
         * Progress in 0f to 1f.
         * If the download source doesn't response "Content-Length" header, you will always receive -1f.
         */
        suspend fun progressChannel(): ReceiveChannel<Float>

        /**
         * Wait for download finished
         * @return Downloaded bytes length
         * @throws Exception
         */
        suspend fun await(): Long
    }

    interface Queue {
        val children: List<DownloadTask>
        suspend fun newTask(): DownloadTask
    }

    val defaultQueue: Queue = Impl()

    private class Impl : Queue {
        override val children: List<DownloadTask>
            get() = emptyList()

        override suspend fun newTask(): DownloadTask {
            TODO()
        }
    }

    // TODO 多线程下载
    fun download(url: String, output: OutputStream): DownloadTask {
        return object : DownloadTask {
            private val scope = CoroutineScope(Dispatchers.IO)
            private val progressChannel = Channel<Float>(Channel.UNLIMITED)
            private val job = scope.async {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.doOutput = false
                conn.connect()
                val contentLength = conn.getHeaderField("Content-Length").toLongOrNull() ?: -1
                val input = conn.inputStream
                progressChannel.send(if (contentLength == -1L) -1f else 0f)
                var bytesCopied: Long = 0
                val buffer = ByteArray(8 * 1024)
                var bytes = input.read(buffer)
                while (bytes >= 0) {
                    output.write(buffer, 0, bytes)
                    bytesCopied += bytes
                    progressChannel.send(if (contentLength == -1L) -1f else bytesCopied.toFloat() / contentLength)
                    bytes = input.read(buffer)
                }
                conn.disconnect()
                progressChannel.close()
                bytesCopied
            }

            override suspend fun progressChannel(): ReceiveChannel<Float> = progressChannel

            override suspend fun await(): Long {
                return job.await()
            }
        }
    }
}