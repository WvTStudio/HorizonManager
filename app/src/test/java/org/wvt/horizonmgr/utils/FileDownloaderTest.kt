package org.wvt.horizonmgr.utils

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class FileDownloaderTest {
    lateinit var testFile: File

    @BeforeTest
    fun setup() {
        testFile = File("test")
    }

    @Test
    fun testDownloadPackage() = runBlocking {
        val task =
            FileDownloader.newTask("https://cdn.jsdelivr.net/gh/WvTStudio/horizon-cloud-config@master/innercore/part0011")
        task.setOutput(testFile.outputStream().buffered())
        val total = task.connect()
        println("Total: $total")
        val job = launch {
            task.progress.collect { println(it) }
        }
        task.download()
        job.cancelAndJoin()
    }

    @AfterTest
    fun clean() {
        testFile.delete()
    }
}