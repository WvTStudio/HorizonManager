package org.wvt.horizonmgr.service.hzpack

import org.json.JSONObject

data class PackageManifest(
    val game: String,
    val gameVersion: String,
    val pack: String,
    val packVersion: String,
    val packVersionCode: Int,
    val developer: String,
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
                    for ((k, v) in descriptions) { put(k, v) }
                })
            }.toString(4)
        }
    }
}