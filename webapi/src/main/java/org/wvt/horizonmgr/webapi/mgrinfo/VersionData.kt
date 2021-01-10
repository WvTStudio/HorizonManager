package org.wvt.horizonmgr.webapi.mgrinfo

data class VersionData internal constructor(
    val channelName: String,
    val versionCode: Int,
    val versionName: String,
    val changeLog: String,
)