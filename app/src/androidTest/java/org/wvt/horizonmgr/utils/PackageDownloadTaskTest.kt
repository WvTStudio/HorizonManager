package org.wvt.horizonmgr.utils

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.wvt.horizonmgr.webapi.pack.OfficialPackageCDNRepository

class PackageDownloadTaskTest {
    @Test
    fun test(): Unit = runBlocking {
        val targetDir = InstrumentationRegistry.getInstrumentation().targetContext
            .filesDir.resolve("test_dir").also { it.mkdir() }

        val pkg = OfficialPackageCDNRepository().getAllPackages().first()
        val task = PackageDownloadTask(pkg, targetDir)
        launch {
            launch {
                task.mergeState.collect {
                    println(it)
                }
            }
            launch {
                task.graphicsDownloadState.collect {
                    println(it)
                }
            }
            launch {
                task.chunkDownloadState.collect {
                    when (it) {
                        is PackageDownloadTask.ChunkDownloadState.Downloading -> launch {
                            it.chunks.forEach {
                                launch {
                                    it.collect { println(it) }
                                }
                            }
                        }
                        else -> println(it)
                    }
                }
            }
        }
        task.run()
        targetDir.deleteRecursively()
    }
}