package org.wvt.horizonmgr.service.webapi2.modmodule

import kotlinx.coroutines.channels.ReceiveChannel
import org.wvt.horizonmgr.service.webapi2.usermodule.User
import java.net.URL


interface ICCNModRepository: ModRepository {
    override suspend fun getRecommendMods(): ReceiveChannel<ICCNMod>
}

interface ICCNMod: Mod {
    override suspend fun getDescription(): String
    override suspend fun getName(): String
    suspend fun getDeveloper(): User
    suspend fun getDownloadLink(): URL
}
