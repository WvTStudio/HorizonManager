package org.wvt.horizonmgr.webapi.pack

import kotlinx.coroutines.channels.ReceiveChannel
import org.wvt.horizonmgr.webapi.mod.User
import java.net.URL

/**
 * TODO-Proposal 这是以后要实现的 ICCN 仓库，和 ICCN 社区绑定
 */
interface ICCNPackageRepository {
    suspend fun getRecommendPacks(): ReceiveChannel<ICCNPackage>
    suspend fun search(text: String): ReceiveChannel<ICCNPackage>
}

interface ICCNPackage {
    suspend fun getName(): String
    suspend fun getDescription(): String
    suspend fun getDownloadURL(): URL

    suspend fun getIcon(): URL
    suspend fun getPreviewImage(): List<URL>

    suspend fun getComments(): ReceiveChannel<PackageComment>
    suspend fun sendComment(content: String)
}

interface PackageComment {
    suspend fun getSender(): User
    suspend fun getContent(): String
}