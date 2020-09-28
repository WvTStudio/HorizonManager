package org.wvt.horizonmgr.service.webapi2.packagemodule

interface PackageModule {
    suspend fun getHZRepository(): HzPackageRepository
    suspend fun getOfficialRepository(): OfficialPackageRepository
}

interface PackageRepository {}

interface Package {
    suspend fun getName(): String
    suspend fun getDescription(): String
}