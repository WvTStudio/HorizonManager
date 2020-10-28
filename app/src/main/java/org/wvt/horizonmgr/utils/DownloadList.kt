package org.wvt.horizonmgr.utils

import java.io.File

object DownloadList {
    // TODO: 2020/10/27

    interface DownloadTask {
        /**
         * Progress in 0f to 1f.
         * If the download source doesn't response "Content-Length" header, you will always receive -1f.
         */
        fun getProgress(): Float

        fun getState()

        /**
         * Wait for download finished
         * @return Downloaded bytes length
         * @throws Exception
         */
        suspend fun await(): Long
    }

    interface DownloadedTask {
        fun getFile(): File
        fun length(): Long
    }

    fun newTask(url: String) {
        // TODO: 2020/10/27
    }
}