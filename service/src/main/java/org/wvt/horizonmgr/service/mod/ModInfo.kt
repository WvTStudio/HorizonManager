package org.wvt.horizonmgr.service.mod

import org.json.JSONObject

/**
 * mod.info 文件
 */
data class ModInfo(
    val name: String,
    val author: String,
    val version: String,
    val description: String
) {
    companion object {
        fun fromJson(jsonStr: String): ModInfo {
            return with(JSONObject(jsonStr)) {
                ModInfo(
                    name = getString("name"),
                    author = getString("author"),
                    version = getString("version"),
                    description = getString("description")
                )
            }
        }

        fun ModInfo.toJson(): String {
            return JSONObject().apply {
                put("name", name)
                put("author", author)
                put("version", version)
                put("description", description)
            }.toString(4)
        }
    }
}