package org.wvt.horizonmgr.service

import android.os.Environment
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.wvt.horizonmgr.utils.CoroutineZip

class CoroutineZipTest : TestCase() {
    fun testZipInfo() {
        runBlocking {
            launch(Dispatchers.IO) {
                val storage = Environment.getExternalStorageDirectory()
                val base = storage.resolve("ADM").resolve("mgr")
                val task = CoroutineZip.unzip(base.resolve("base.apk"), base.resolve("unzip"))
                task.progressChannel().consumeAsFlow().conflate().collect {
                    delay(100)
                    println(it)
                }
                task.await()
            }
        }
    }
}