package org.wvt.horizonmgr.webapi.mod

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.datetime.Instant
import java.net.URL

/**
 * TODO-Proposal 这是以后需要做的仓库
 */
interface ICCNRepository {
    suspend fun getRecommendMods(): ReceiveChannel<ICCNMod>
    suspend fun search(text: String): ReceiveChannel<ICCNMod>
}

interface ICCNMod {
    suspend fun getName(): String
    suspend fun getDescription(): String

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

/**
 * TODO-Proposal 以后该类要和 ICCN 社区绑定
 */
interface User {

}