package org.wvt.horizonmgr.webapi.mgrmodule

interface MgrModule {
    suspend fun getDonateList(): List<DonateRecord>
    suspend fun getQQGroupList(): List<QQGroup>
}

interface DonateRecord {
    fun donorName(): String
    fun money(): Int
}

interface QQGroup {
    fun getName(): String
    fun getDescription(): String
    fun getUrlLink(): String
    fun getAvatarUrl(): String
    fun getStatus(): String
}