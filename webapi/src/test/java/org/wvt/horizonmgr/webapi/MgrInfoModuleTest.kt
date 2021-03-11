package org.wvt.horizonmgr.webapi

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.wvt.horizonmgr.webapi.mgrinfo.MgrInfoModule
import java.math.BigDecimal

class MgrInfoModuleTest {
    private val mgrInfo = MgrInfoModule()

    @Test
    fun testGetDonates() = runBlocking {
        mgrInfo.getDonateList().forEach {
            println("name: ${it.donorName}, money: ${it.money.toBigDecimal().divide(BigDecimal(100))}")
        }
    }

    @Test
    fun testGetQQGroupList() = runBlocking {
        mgrInfo.getQQGroupList().forEach {
            println(it)
        }
    }

    @Test
    fun testGetVersions() = runBlocking {
        mgrInfo.getVersionChannels().forEach {
            it.getVersions().forEach {
                println(it.getData())
            }
        }
    }

    @Test
    fun testGetChannelByName() = runBlocking {
        val releaseChannel = mgrInfo.getChannelByName("release") ?: error("")
        println("Latest version: " + releaseChannel.latestVersion())
    }
}