package org.wvt.horizonmgr.webapi.mod

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import org.json.JSONException
import org.json.JSONObject
import org.wvt.horizonmgr.webapi.JsonParseException
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.ServiceException
import org.wvt.horizonmgr.webapi.forEach
import java.io.IOException

/**
 * 汉化组仓库
 */
class ChineseModRepository {
    private val client: HttpClient = HttpClient(CIO)
    /**
     * 获取所有模组信息
     *
     * 向 `https://dev.adodoz.cn/api/mod/list` 发送 GET 请求
     *
     * 成功时返回的 JSON 格式如下，其他情况则失败
     *
     * ```json
     * {
     *   "state": "success",
     *   "info": [],
     *   "data": [
     *     {
     *       "id": <int>,
     *       "name": <str>,
     *       "version": <str>, // 版本名，没有标准格式
     *       "icon": <str>, // 模组的图标，这是 `https://dev.adodoz.cn/` 的路径，
     *       "pic": { // 预览图，这些都是 `https://dev.adodoz.cn/` 的路径
     *         "0": <str>,
     *         "1": <str>,
     *         ...
     *         "n": <str>
     *       }
     *       "desc": <str>, // HTML 格式的描述
     *       "absrtact": <str>, // 纯文本形式的摘要（写后端的人拼错了，应该是 abstract）
     *       "time": <str>, // 时间，没有固定格式
     *       "size": <ing>, // 大小，单位 Byte
     *       "md5": <str>,
     *       "download": <int>, // 下载量
     *       "user": <int>,
     *       "permiss": <int>,
     *       "plate": <int>,
     *       "copyright": <str>,
     *       "state": <int>,
     *       "iden": <str>,
     *       "dis": <str>
     *     },
     *     ...
     *   ]
     * }
     * ```
     */
    suspend fun getAllMods(): List<ChineseMod> {
        val jsonStr = try {
            client.get<String>("https://dev.adodoz.cn/api/mod/list")
        }catch (e: IOException) {
            throw NetworkException("获取汉化源 Mod 列表失败", e)
        }
        val result = mutableListOf<ChineseMod>()
        try {
            val json = JSONObject(jsonStr)
            if (json.getString("state") != "success") throw ServiceException("获取失败")

            json.getJSONArray("data").forEach<JSONObject> { item ->
                with(item) {
                    val state = item.getInt("state")
                    if (state != 1) return@forEach // 脏数据

                    val pictures = getJSONObject("pic").let { pic ->
                        pic.keys().asSequence().map { key ->
                            pic.getString(key)
                        }.toList()
                    }

                    val data = ChineseMod(
                        httpClient = client,
                        id = getInt("id"),
                        name = getString("name"),
                        description = getString("absrtact"),
                        versionName = getString("version"),
                        icon = getString("icon"),
                        pictures = pictures,
                        time = getString("time")
                    )
                    result.add(data)
                }
            }
        } catch (e: JSONException) {
            throw JsonParseException(jsonStr, e)
        }

        return result
    }

    suspend fun getModById(id: Int): ChineseMod? {
        return getAllMods().firstOrNull { it.id == id }
    }
}

/**
 * 代表一个汉化组仓库中的 Mod
 */
class ChineseMod internal constructor(
    private val httpClient: HttpClient,
    val id: Int,
    val name: String,
    val description: String,
    val versionName: String,
    val time: String,
    icon: String,
    pictures: List<String>,
) {
    companion object {
        private const val BASE = "https://dev.adodoz.cn"
    }

    val icon = BASE + icon
    val pictures = pictures.map { BASE + it }

    /**
     * 获取该 Mod 的下载地址
     *
     * 向 `https://dev.adodoz.cn/api/mod/download` 发送 GET 请求，`id` 参数为要获取的模组的 ID。
     *
     * 成功时返回如下格式的 Json，其他情况则失败
     *
     * ```json
     * {
     *   "state": "success",
     *   "info": [],
     *   "data": {
     *     "name": <str>, // 文件名（不带文件后缀）
     *     "url": <str> // 不是真正 URL，而是 `https://dev.adodoz.cn/` 的资源路径
     *   }
     * }
     * ```
     */
    suspend fun getDownloadURL(): DownloadURL {
        val jsonStr = httpClient.get<String>("https://dev.adodoz.cn/api/mod/download?id=$id")

        val fileName: String
        val path: String

        try {
            val jsonRoot = JSONObject(jsonStr)
            val state = jsonRoot.getString("state")
            if (state == "success") {
                val data = jsonRoot.getJSONObject("data")
                fileName = data.getString("name")
                path = data.getString("url")
            } else {
                throw ServiceException("获取失败，state: $state")
            }
        } catch (e: JSONException) {
            throw JsonParseException(jsonStr, e)
        }

        val filePostfix = path.substringAfterLast(".")

        return DownloadURL(
            fileName = "$fileName.$filePostfix",
            url = BASE + path
        )
    }

    /**
     *
     */
    data class DownloadURL(
        /**
         * 文件名（不含后缀）
         */
        val fileName: String,
        /**
         * 下载地址
         */
        val url: String
    )
}