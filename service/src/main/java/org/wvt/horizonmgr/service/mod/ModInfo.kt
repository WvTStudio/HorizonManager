package org.wvt.horizonmgr.service.mod

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * mod.info 文件
 */
@Serializable
data class ModInfo(
    val name: String,
    val author: String,
    val version: String,
    val description: String
) {
    companion object {
        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        fun fromJson(jsonStr: String): ModInfo {
            return json.decodeFromString(jsonStr)
        }

        fun ModInfo.toJson(): String {
            return json.encodeToString(this)
        }
    }
}