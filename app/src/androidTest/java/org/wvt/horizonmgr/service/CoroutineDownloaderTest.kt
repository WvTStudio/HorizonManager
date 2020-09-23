package org.wvt.horizonmgr.service

import android.os.Environment
import android.util.Log
import junit.framework.TestCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import org.wvt.horizonmgr.utils.CoroutineDownloader

class CoroutineDownloaderTest : TestCase() {
    fun testDownload() = runBlocking<Unit> {
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        Log.d("TestDownload", downloadDir.absolutePath)
        val file = downloadDir.resolve("download.zip").outputStream()
        WebAPI.getPackages().first().chunks.sortedBy { it.chunkIndex }.forEach { chunk ->
            val task = CoroutineDownloader.download(chunk.url, file)
            task.progressChannel().receiveAsFlow().conflate().collect {
                println("Downloaded: $it")
                delay(100)
            }
            task.await()
        }
    }
}