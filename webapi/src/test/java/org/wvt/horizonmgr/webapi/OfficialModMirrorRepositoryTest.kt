package org.wvt.horizonmgr.webapi

import kotlinx.coroutines.runBlocking
import org.wvt.horizonmgr.webapi.mod.OfficialModMirrorRepository
import kotlin.test.Test

class OfficialModMirrorRepositoryTest {
    private val repository = OfficialModMirrorRepository()

    @Test
    fun testGetAllMods() = runBlocking {
        repository.getAllMods().forEach {
            println(it)
        }
    }
}