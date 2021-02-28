package org.wvt.horizonmgr.webapi

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.wvt.horizonmgr.webapi.mod.OfficialModMirrorRepository

class OfficialModMirrorRepositoryTest {
    private val repository = OfficialModMirrorRepository()

    @Test
    fun testGetAllMods() = runBlocking {
        repository.getAllMods().forEach {
            println(it)
        }
    }
}