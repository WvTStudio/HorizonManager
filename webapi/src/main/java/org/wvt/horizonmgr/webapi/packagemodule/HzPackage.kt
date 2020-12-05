package org.wvt.horizonmgr.webapi.packagemodule

import java.net.URL

interface HzPackageRepository : PackageRepository {
    suspend fun getAll(): List<HzPackage>
}

interface HzPackage : Package {
    override suspend fun getName(): String
    override suspend fun getDescription(): String
    suspend fun getUUID(): String
    suspend fun getGraphicsUrl(): URL
    suspend fun getDownloadChunks(): List<URL>
    suspend fun isRecommend(): Boolean
    suspend fun getManifestStr(): String
}