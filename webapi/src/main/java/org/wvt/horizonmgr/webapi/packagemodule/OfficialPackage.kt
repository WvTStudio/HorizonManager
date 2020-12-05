package org.wvt.horizonmgr.webapi.packagemodule

import kotlinx.coroutines.channels.ReceiveChannel
import org.wvt.horizonmgr.webapi.usermodule.User
import java.net.URL

interface OfficialPackageRepository {
    suspend fun getRecommendPacks(): ReceiveChannel<OfficialPackage>
    suspend fun search(text: String): ReceiveChannel<OfficialPackage>
}

interface OfficialPackage : Package {
    override suspend fun getName(): String
    override suspend fun getDescription(): String
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