package org.wvt.horizonmgr.service.hzpack

import org.json.JSONObject

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
     * 分包的版本命
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
    val descriptions: Map<String, String>,
) {
    companion object {
        fun fromJson(jsonStr: String): PackageManifest {
            return with(JSONObject(jsonStr)) {
                val desJson = getJSONObject("description")
                val description = mutableMapOf<String, String>()
                desJson.keys().forEach {
                    description[it] = desJson.getString(it)
                }
                PackageManifest(
                    game = getString("game"),
                    gameVersion = getString("gameVersion"),
                    pack = getString("pack"),
                    packVersion = getString("packVersion"),
                    packVersionCode = getInt("packVersionCode"),
                    developer = getString("developer"),
                    descriptions = description
                )
            }
        }

        fun PackageManifest.toJson(): String {
            return JSONObject().apply {
                put("game", game)
                put("gameVersion", gameVersion)
                put("pack", pack)
                put("packVersion", packVersion)
                put("packVersionCode", packVersionCode)
                put("developer", developer)
                put("descriptions", JSONObject().apply {
                    for ((k, v) in descriptions) {
                        put(k, v)
                    }
                })
            }.toString(4)
        }
    }
}

fun PackageManifest.recommendDescription(): String {
    return this.descriptions["zh"] ?: this.descriptions["en"]
    ?: this.descriptions.asIterable().firstOrNull()?.value ?: "No description"
}