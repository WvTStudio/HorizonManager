package org.wvt.horizonmgr.service

import junit.framework.TestCase
import kotlinx.coroutines.runBlocking

class WebAPITest : TestCase() {
    fun testVersion() {
        runBlocking {
            println(WebAPI.getLatestAppVersion())
        }
    }

    fun testLogin() {
        runBlocking {
            println(WebAPI.login("2368442877@qq.com", "w150873037"))
        }
    }

    fun testRegister() {
        runBlocking {
            val id = 5
            val response =
                try {
                    WebAPI.register(
                        "apitest$id",
                        "apitest$id@xxxapitestxxx.com",
                        "testtesttesttest"
                    )
                } catch (e: WebAPI.RegisterException) {
                    e.errors.forEach(::println)
                }
            println(response)
        }
    }

    fun testRegisterInvalidInput() {
        runBlocking {
            try {
                WebAPI.register(
                    "哈哈哈",
                    "啦啦啦",
                    "呜呜呜呜呜"
                )
            } catch (e: WebAPI.RegisterException) {
                e.errors.forEach(::println)
            }
        }
    }

    fun testDonate() {
        runBlocking {
            println(WebAPI.getDonates())
        }
    }

    fun testQQ() {
        runBlocking {
            println(WebAPI.getQQGroupList())
        }
    }

    fun testOfficialMods() {
        runBlocking {
            WebAPI.getModsFromOfficial().forEach {
                println(it)
                it.downloadUrl()
            }
        }
    }

    fun testCNMods() {
        runBlocking {
            WebAPI.getModsFromCN().forEach {
                println(it)
                it.downloadUrl()
            }
        }
    }

    fun testGetPackages() {
        runBlocking {
            assertEquals(WebAPI.getPackages().size, 2)
        }
    }

    fun testDownloadPackage() {
        runBlocking {
            WebAPI.downloadPackage(WebAPI.getPackages().first()).await()
        }
    }
}