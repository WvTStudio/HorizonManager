package org.wvt.horizonmgr.service.hzpack

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PackageManifest(
    /**
     * MC 游戏名
     */
    val game: String,
    /**
     * MC 游戏版本
     */
    val gameVersion: String,
    /**
     * 分包的名称
     */
    val pack: String,
    /**
     * 分包的版本名
     */
    val packVersion: String,
    /**
     * 分包的版本号
     */
    val packVersionCode: Int,
    /**
     * 开发者
     */
    val developer: String,
    /**
     * 分包的描述，key 为语言，如 "en"
     */
    @SerialName("description")
    val descriptions: Map<String, String>,
) {
    companion object {
        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        fun fromJson(jsonStr: String): PackageManifest {
            return json.decodeFromString(jsonStr)
        }

        fun PackageManifest.toJson(): String {
            return json.encodeToString(this)
        }
    }
}

fun PackageManifest.recommendDescription(): String {
    return descriptions["zh"] ?: descriptions["en"] ?: descriptions["gb"]
    ?: descriptions["ru"]
    ?: descriptions.asIterable().firstOrNull()?.value
    ?: "No description"
}