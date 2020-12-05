package org.wvt.horizonmgr.webapi.modmodule

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.datetime.Instant
import org.wvt.horizonmgr.webapi.usermodule.User
import java.net.URL

/**
 * 这是以后需要做的仓库
 */
interface OfficialRepository : ModRepository {
    override suspend fun getRecommendMods(): ReceiveChannel<Mod>
    suspend fun search(text: String): ReceiveChannel<OfficialMod>
}

interface OfficialMod : Mod {
    override suspend fun getName(): String
    override suspend fun getDescription(): String

    suspend fun getDeveloper(): User
    suspend fun getVersionCode(): Int
    suspend fun getVersionName(): String

    suspend fun getCreateTime(): Instant
    suspend fun getUpdateTime(): Instant
    suspend fun getHeat(): Long

    suspend fun getIcon(): URL
    suspend fun getPreviewImages(): List<URL>

    suspend fun getDownloadLink(): URL

    suspend fun getComment(id: String): ModComment
    suspend fun getComments(): ReceiveChannel<ModComment>

    suspend fun sendComment(content: String)
    suspend fun reply(comment: ModComment, content: String)
}

interface ModComment {
    suspend fun getCreator(): User
    suspend fun getCreateTime(): Instant
    suspend fun getContent(): String
    suspend fun getReplies(): ReceiveChannel<ModComment>
}