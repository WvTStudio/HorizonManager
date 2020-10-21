package org.wvt.horizonmgr.service.webapi2.modmodule

import kotlinx.coroutines.channels.ReceiveChannel

interface ModModule {
    suspend fun getICCNRepository(): ICCNModRepository
    suspend fun getHzOfficialRepository(): ICCNModRepository
    suspend fun getOfficialRepository(): OfficialRepository
}

interface ModRepository {
    suspend fun getRecommendMods(): ReceiveChannel<Mod>
}


interface Mod {
    suspend fun getName(): String

    /**
     * 返回的不一定是纯文本
     */
    suspend fun getDescription(): String
}