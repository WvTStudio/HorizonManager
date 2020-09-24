package org.wvt.horizonmgr.service.webapi2.modmodule

import kotlinx.coroutines.channels.ReceiveChannel

interface HzOfficialModRepository: ModRepository {
    override suspend fun getRecommendMods(): ReceiveChannel<HzOfficialMod>
}

interface HzOfficialMod: Mod {
    override suspend fun getName(): String
    override suspend fun getDescription(): String

    suspend fun isOptimized(): Boolean
    suspend fun getDownloadCount(): Int
}
