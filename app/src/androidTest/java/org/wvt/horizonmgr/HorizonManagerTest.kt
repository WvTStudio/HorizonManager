package org.wvt.horizonmgr

/*
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.wvt.horizonmgr.service.hzpack.HorizonManager


class HorizonManagerTest {
    @Test
    fun getInstalledPackagesTest() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val packages = HorizonManager(appContext).getInstalledPackages()
        if (packages.isEmpty()) {
            println("Empty")
        } else {
            packages.forEach {
                println("Package: " + it.getName())
                it.getMods().forEachIndexed { index, installedMod ->
                    println("Mod[$index]: ${installedMod.getName()}, enable: ${installedMod.isEnabled()}")
                    if (installedMod.isEnabled()) {
                        installedMod.disable()
                    } else {
                        installedMod.enable()
                    }
                }
            }
        }
    }
}*/
