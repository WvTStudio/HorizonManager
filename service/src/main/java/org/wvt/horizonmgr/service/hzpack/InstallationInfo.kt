package org.wvt.horizonmgr.service.hzpack

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class InstallationInfo(
    /**
     * 分包的 UUID
     */
    @SerialName("uuid")
    val packageId: String,
    @SerialName("timestamp")
    val timeStamp: Long,
    /**
     * 用户自定义的名称
     */
    val customName: String?,
    /**
     * 分包安装时创建的 UUID，用于区别设备内安装的分包
     */
    val internalId: String
) {
    companion object {
        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        fun fromJson(jsonStr: String): InstallationInfo {
            return json.decodeFromString(jsonStr)
        }
    }

    fun toJson(): String {
        return json.encodeToString(this)
    }
}