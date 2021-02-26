package org.wvt.horizonmgr.service.pack

import org.json.JSONObject

data class InstallationInfo(
    val packageId: String,
    val timeStamp: Long,
    val customName: String,
    val internalId: String
) {
    companion object {
        fun fromJson(jsonStr: String): InstallationInfo? {
            return try {
                with(JSONObject(jsonStr)) {
                    InstallationInfo(
                        packageId = getString("uuid"),
                        timeStamp = getLong("timestamp"),
                        customName = getString("customName"),
                        internalId = getString("internalId")
                    )
                }
            } catch (e: Exception) {
                null
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