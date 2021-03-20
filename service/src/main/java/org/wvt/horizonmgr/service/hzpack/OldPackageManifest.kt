package org.wvt.horizonmgr.service.hzpack

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class OldPackageManifest(
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
    val packVersion: String? = null,
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
    val description: String
) {
    companion object {
        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        fun fromJson(jsonStr: String): OldPackageManifest {
            return json.decodeFromString(jsonStr)
        }

        fun OldPackageManifest.toJson(): String {
            return json.encodeToString(this)
        }

        fun OldPackageManifest.toPackageManifest(): PackageManifest {
            return PackageManifest(game, gameVersion, pack, packVersion ?: packVersionCode.toString(), packVersionCode, developer, mapOf("gb" to description))
        }
    }
}
