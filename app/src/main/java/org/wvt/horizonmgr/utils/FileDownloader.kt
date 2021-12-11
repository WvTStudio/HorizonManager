package org.wvt.horizonmgr.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume

object FileDownloader {
    interface Task {
        val progress: StateFlow<Long>
        fun getSize(): Long

        /**
         * Return size of the file
         */
        suspend fun connect(): Long
        fun setOutput(output: OutputStream)

        /**
         * Download
         */
        suspend fun download()
    }

    private class TaskImpl(
        private val url: String
    ) : Task {
        override val progress = MutableStateFlow(0L)
        private var connected = false
        private var size: Long = 0L
        private lateinit var conn: HttpURLConnection
        private lateinit var output: OutputStream

        override suspend fun connect(): Long {
            if (connected) error("Connected")
            conn = URL(url).openConnection() as HttpURLConnection
            conn.doOutput = false
            conn.connect()
            connected = true
            size = conn.getHeaderField("Content-Length")?.toLongOrNull() ?: -1
            return size
        }

        override fun getSize(): Long {
            if (connected) return size
            else error("Not connected")
        }

        override fun setOutput(output: OutputStream) {
            this.output = output
        }

        override suspend fun download() = withContext(Dispatchers.IO) {
            if (!connected) error("Not connected")

            var bytesCopied = 0L

            val byteArray = ByteArray(1024)
            val input = conn.inputStream.buffered(1024)
            while (isActive) {
                val size = suspendCancellableCoroutine<Int> {
                    it.resume(input.read(byteArray, 0, byteArray.size))
                }
                if (size <= 0) break
                suspendCancellableCoroutine<Unit> {
                    output.write(byteArray, 0, size)
                    it.resume(Unit)
                }
                bytesCopied += size
                progress.emit(bytesCopied)
            }
        }
    }

    fun newTask(url: String): Task {
        return TaskImpl(url)
    }
}