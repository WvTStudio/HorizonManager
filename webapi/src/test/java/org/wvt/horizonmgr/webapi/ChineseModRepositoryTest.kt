package org.wvt.horizonmgr.webapi

import kotlinx.coroutines.runBlocking
import org.wvt.horizonmgr.webapi.mod.ChineseModRepository
import kotlin.test.Test

class ChineseModRepositoryTest {
    private val repository = ChineseModRepository()

    @Test
    fun testGetAllMods() = runBlocking {
        repository.getAllMods().forEach {
            println("id: ${it.id}, name: ${it.name}, download: ${it.getDownloadURL()}")
        }
    }

    @Test
    fun testGetModById() = runBlocking {
        val mod = repository.getModById(11)!!
        assert(mod.name != "更多宝石矿物")
    }
}