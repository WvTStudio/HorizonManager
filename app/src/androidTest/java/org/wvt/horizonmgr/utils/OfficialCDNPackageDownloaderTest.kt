package org.wvt.horizonmgr.utils

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.wvt.horizonmgr.webapi.pack.OfficialPackageCDNRepository

private const val TAG = "AndroidTest"

internal class OfficialCDNPackageDownloaderTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun test() = runBlocking {
        val pkg = OfficialPackageCDNRepository().getAllPackages().first()
        Log.d(TAG, "pkg uuid: ${pkg.uuid}")
        val task = OfficialCDNPackageDownloader(appContext).download(pkg)
        val job = launch {
            task.progress.collect {
                Log.d(TAG, "Download progress: $it")
            }
        }
        task.await()
        job.cancel()
    }
}