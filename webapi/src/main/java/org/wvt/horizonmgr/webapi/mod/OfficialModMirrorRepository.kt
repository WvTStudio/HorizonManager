package org.wvt.horizonmgr.webapi.mod

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.wvt.horizonmgr.webapi.JsonParseException
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.forEachIndexed

/**
 * Horizon 官方 Mod 仓库的 ICCN 镜像
 */
class OfficialModMirrorRepository {
    private val client: HttpClient = HttpClient(CIO)

    /**
     * 获取所有模组
     *
     * 下载 `https://adodoz.cn/mods/allmodinfo.json` 文件
     * 格式如下
     *
     * ```json
     * [
     *   {
     *     "id": <int>, // Mod 的 ID
     *     "title": <str>,
     *     "description": <str>,
     *     "icon": <str>, // 格式如 1.png, 2.png, 3.png
     *     "version_name": <str>,
     *     "horizon_optimized": <int>,
     *     "last_update": <str|null>, // 最后一次更新时间，如果存在，则格式为：[YYYY-MM-DD HH:MM:SS]，如 "2020-09-14 19:44:38",还可能是 "null"
     *     "vip": <int>,
     *     "pack": <int>,
     *     "multiplayer": <int>,
     *     "likes": <int>,
     *     "dislikes": <int>,
     *     "liked": <int>,
     *     "disliked": <int>
     *   }
     * ]
     * ```
     *
     * 数组中存在一个这样的固定对象，这是为旧版本 IC 做的更新提示，现在应当作脏数据处理
     *
     * ```json
     * {
     *   "id": -100,
     *   "title": "Update Inner Core for Horizon now!",
     *   "description": "Use the latest features of your favorite mods and the best modding environment ever!",
     *   "version_name": "",
     *   "last_update": null,
     *   "icon": "horizon_update.png",
     *   "vip": true,
     *   "pack": false,
     *   "likes": 999
     * }
     * ```
     */
    suspend fun getAllMods(): List<OfficialMirrorMod> {
        val jsonStr = try {
            client.get<String>("https://adodoz.cn/mods/allmodinfo.json")
        } catch (e: Exception) {
            throw NetworkException("Network error: ${e.message}", e)
        }

        val result = mutableListOf<OfficialMirrorMod>()
        try {
            val jsonArray = JSONArray(jsonStr)

            jsonArray.forEachIndexed<JSONObject> { index, item ->
                with(item) {
                    val id = getInt("id")
                    if (id < 0) return@forEachIndexed // 脏数据

                    val mod = OfficialMirrorMod(
                        id = id,
                        title = getString("title"),
                        description = getString("description"),
                        icon = getString("icon"),
                        versionName = getString("version_name"),
                        horizonOptimized = getInt("horizon_optimized") == 1,
                        lastUpdate = try {
                            getString("last_update").takeIf { it.isNotBlank() && it != "null" }
                        } catch (e: Exception) {
                            null
                        },
                        multiplayer = getInt("multiplayer") == 1,
                        likes = getInt("likes"),
                        dislikes = getInt("dislikes")
                    )
                    result.add(mod)
                }
            }
        } catch (e: JSONException) {
            throw JsonParseException(jsonStr, e)
        }

        return result
    }
}

/**
 * 表示一个官方源 CDN 镜像中的 Mod
 * Mod 的下载地址为：`https://adodoz.cn/mods/` + Mod 的 ID + `.zip`
 * Icon Image 的 URL 为：`https://adodoz.cn/mods/img/` + `icon`
 */
data class OfficialMirrorMod internal constructor(
    val id: Int,
    val title: String,
    val description: String,
    private val icon: String,
    val versionName: String,
    val horizonOptimized: Boolean,
    val lastUpdate: String?,
    val multiplayer: Boolean,
    val likes: Int,
    val dislikes: Int
) {
    val downloadUrl = "https://adodoz.cn/mods/$id.zip"
    val iconUrl = "https://adodoz.cn/mods/img/$icon"
}