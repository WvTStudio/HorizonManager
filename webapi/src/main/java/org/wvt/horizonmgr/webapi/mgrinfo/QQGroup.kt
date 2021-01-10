package org.wvt.horizonmgr.webapi.mgrinfo

data class QQGroup internal constructor(
    val name: String,
    val description: String,
    val avatarUrl: String,
    val urlLink: String,
    val status: String
)