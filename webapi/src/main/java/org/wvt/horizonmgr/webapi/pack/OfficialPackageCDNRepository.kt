package org.wvt.horizonmgr.webapi.pack

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import org.json.JSONException
import org.json.JSONObject
import org.wvt.horizonmgr.webapi.JsonParseException
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.forEach
import java.io.IOException

/**
 * 官方分包仓库的 CDN
 */
class OfficialPackageCDNRepository {
    private val httpClient = HttpClient(CIO)

    /**
     * ```json
     * {
     *   "packs": [
     *     {
     *       "uuid": <str>,
     *       "changelog": {
     *         "en": <str>, // 一个 HTML 格式的更新日志的 URL
     *         "ru": <str>,
     *         ...
     *       },
     *       "graphics": <str>, // 预览图压缩包的 URL
     *       "manifest": <str>, // 一个 Manifest Json 文件的 URL
     *       "package": { // 各个区块
     *         "part0001": <str>,
     *         "part0002": <str>,
     *         ...
     *         "partABCD": <str>
     *       }
     *     },
     *     ...
     *   ],
     *   "suggestion": [
     *     <str>, // 推荐分包的 UUID
     *     ...
     *   ]
     * }
     * ```
     */
    @OptIn(ExperimentalStdlibApi::class)
    suspend fun getAllPackages(): List<OfficialCDNPackage> {
        val jsonStr = try {
            httpClient.get<String>("https://cdn.jsdelivr.net/gh/WvTStudio/horizon-cloud-config@master/packs.json")
        } catch (e: IOException) {
            throw NetworkException("获取分包信息失败", e)
        }

        val result = mutableListOf<OfficialCDNPackage>()

        try {
            val json = JSONObject(jsonStr)

            // parse recommended package uuid
            val suggestions = buildList<String> {
                json.getJSONArray("suggestions").forEach(::add)
            }

            // parse packages
            json.getJSONArray("packs").forEach<JSONObject> {
                val uuid = it.getString("uuid")
                val graphicsUrl = it.getString("graphics")
                val manifestUrl = it.getString("manifest")
                val changelogUrl =
                    it.getJSONObject("changelog").getString("en") // TODO-Proposal 多语言

                // parse file chunks
                val chunks: List<OfficialCDNPackage.Chunk> = it.getJSONObject("package").let {
                    buildList {
                        it.keys().forEach { key ->
                            val url = it.getString(key)
                            // The chunk name is in pattern of "partXXXX", and start at 1
                            // - 1 to make it starts at 0
                            val index = key.removePrefix("part").toInt() - 1
                            add(OfficialCDNPackage.Chunk(index, url))
                        }
                    }.sortedBy { it.index }
                }

                val data = OfficialCDNPackage(
                    httpClient = httpClient,
                    uuid = uuid,
                    graphicsUrl = graphicsUrl,
                    isSuggested = suggestions.contains(uuid),
                    chunks = chunks,
                    changelogUrl = changelogUrl,
                    manifestUrl = manifestUrl
                )

                result.add(data)
            }
        } catch (e: JSONException) {
            throw JsonParseException(jsonStr, e)
        }
        return result
    }
}

class OfficialCDNPackage internal constructor(
    private val httpClient: HttpClient,
    val uuid: String,
    val graphicsUrl: String,
    val isSuggested: Boolean,
    val chunks: List<Chunk>,
    private val changelogUrl: String,
    private val manifestUrl: String
) {
    data class Chunk(
        val index: Int,
        val url: String
    )

    suspend fun getManifest(): String {
        return try {
            httpClient.get(manifestUrl)
        } catch (e: IOException) {
            throw NetworkException("获取清单失败", e)
        }
    }

    suspend fun getChangeLog(): String {
        return try {
            httpClient.get(changelogUrl)
        } catch (e: IOException) {
            throw NetworkException("获取更新日志失败", e)
        }
    }
}