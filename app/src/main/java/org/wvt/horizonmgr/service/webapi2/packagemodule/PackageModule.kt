package org.wvt.horizonmgr.service.webapi2.packagemodule

import kotlinx.coroutines.channels.ReceiveChannel
import org.wvt.horizonmgr.service.webapi2.usermodule.User
import java.net.URL

interface PackageModule {
    suspend fun getHZRepository(): HzPackageRepository
    suspend fun getOfficialRepository(): OfficialPackageRepository
}

interface PackageRepository {

}

interface HzPackageRepository {
    suspend fun getAll(): List<HzPackage>
}

interface HzPackage {
    suspend fun getName(): String
}

interface OfficialPackageRepository {
    suspend fun getRecommendPacks(): ReceiveChannel<OfficialPackage>
    suspend fun search(text: String): ReceiveChannel<OfficialPackage>
}

interface OfficialPackage {
    suspend fun getName(): String
    suspend fun getDescription(): String
    suspend fun getDownloadLink(): String

    suspend fun getIcon(): URL
    suspend fun getPreviewImage(): List<URL>

    suspend fun getComments(): ReceiveChannel<PackageComment>
    suspend fun sendComment(content: String)
}

interface PackageComment {
    suspend fun getSender(): User
    suspend fun getContent(): String
}