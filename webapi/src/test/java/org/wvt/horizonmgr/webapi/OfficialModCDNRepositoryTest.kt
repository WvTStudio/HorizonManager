package org.wvt.horizonmgr.webapi

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.wvt.horizonmgr.webapi.mod.OfficialModCDNRepository

class OfficialModCDNRepositoryTest {
    private val repository = OfficialModCDNRepository()

    @Test
    fun testGetAllMods() = runBlocking {
        repository.getAllMods().forEach {
            println(it)
        }
    }
}