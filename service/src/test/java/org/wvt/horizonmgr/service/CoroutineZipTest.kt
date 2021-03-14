package org.wvt.horizonmgr.service

import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File

class CoroutineZipTest {
    @Test
    fun test() {
        runBlocking {
            CoroutineZip.unzip(File("D:/test/test_2.zip"), File("D:/test/out"), true).await()
        }
    }
}