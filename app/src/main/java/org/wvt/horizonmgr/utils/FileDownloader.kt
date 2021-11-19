package org.wvt.horizonmgr.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object FileDownloader {
    interface Task {
        /**
         * Return size of the file
         */
        suspend fun connect(): Long
        fun setOutput(output: OutputStream)

        /**
         * Start download, return an state flow represents the downloaded bytes.
         */
        fun start(): StateFlow<Long>

        /**
         * Wait for downloading completely, or throws an exception
         */
        suspend fun await()
    }

    private class TaskImpl(
        private val url: String
    ) : Task {
        private val scope = CoroutineScope(Dispatchers.IO)

        private var connected = false
        private var size: Long = 0L
        private lateinit var conn: HttpURLConnection
        private lateinit var job: Deferred<Unit>
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

        fun getSize(): Long {
            if (connected) return size
            else error("Not connected")
        }

        override fun setOutput(output: OutputStream) {
            this.output = output
        }

        override fun start(): StateFlow<Long> {
            if (!connected) error("Not connected")
            val state = MutableStateFlow(0L)

            job = scope.async {
                conn.inputStream.buffered().use { input ->
                    var bytesCopied: Long = 0
                    val buffer = ByteArray(8 * 1024)
                    var bytes = input.read(buffer)

                    while (bytes >= 0 && isActive) {
                        output.write(buffer, 0, bytes)
                        bytesCopied += bytes
                        state.emit(bytesCopied)
                        bytes = input.read(buffer)
                    }

                    conn.disconnect()
                }
            }

            return state.asStateFlow()
        }


        override suspend fun await() {
            job.await()
        }
    }

    fun newTask(url: String): Task {
        return TaskImpl(url)
    }
}