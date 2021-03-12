package org.wvt.horizonmgr.service.hzpack

import org.json.JSONObject

data class InstallationInfo(
    /**
     * 分包的 UUID
     */
    val packageId: String,
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
        fun fromJson(jsonStr: String): InstallationInfo {
            return with(JSONObject(jsonStr)) {
                InstallationInfo(
                    packageId = getString("uuid"),
                    timeStamp = getLong("timestamp"),
                    customName = optString("customName"),
                    internalId = getString("internalId")
                )
            }
        }
    }

    fun toJson(): String {
        return with(JSONObject()) {
            put("uuid", packageId)
            put("timestamp", timeStamp)
            put("customName", customName)
            put("internalId", internalId)
        }.toString(4)
    }
}