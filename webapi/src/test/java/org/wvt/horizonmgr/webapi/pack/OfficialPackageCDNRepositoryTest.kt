package org.wvt.horizonmgr.webapi.pack

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class OfficialPackageCDNRepositoryTest {

    @Test
    fun getAllPackages() = runBlocking {
        val pack = OfficialPackageCDNRepository()
        pack.getAllPackages().forEach {
            println("uuid: ${it.uuid}, chunks: [${it.chunks.joinToString()}], isSuggested: ${it.isSuggested}, graphicsUrl: ${it.graphicsUrl}")
            println(it.getManifest())
            println(it.getChangeLog())
        }
    }
}