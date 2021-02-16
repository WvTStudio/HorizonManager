package org.wvt.horizonmgr.webapi.mgrinfo

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.wvt.horizonmgr.webapi.JsonParseException
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.forEach
import java.io.IOException

/**
 * 代表一个版本通道，通过此类可以获取对应通道的最新版本、所有版本
 */
class VersionChannel internal constructor(
    val channelName: String
) {
    private val client = HttpClient(CIO) {
        HttpResponseValidator { validateResponse { } }
    }

    suspend fun latestVersion(): Version {
        val response = try {
            client.get<String>("https://adodoz.cn/hzmgr/v1/versioninfo.json")
        } catch (e: IOException) {
            throw NetworkException("获取版本信息失败", e)
        }

        try {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                if (channel == channelName) {
                    val versionCode = it.getInt("latestVersionCode")
                    return Version(channelName, versionCode)
                }
            }
        } catch (e: JSONException) {
            throw JsonParseException(response, e)
        }

        error("Server returned the data, but there was no latest version data entry.")
    }

    suspend fun getVersions(): List<Version> {
        val response = try {
            client.get<String>("https://adodoz.cn/hzmgr/v1/changelogs.json")
        } catch (e: IOException) {
            throw NetworkException("获取更新日志失败", e)
        }
        val result = mutableListOf<Version>()
        try {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                if (channel == channelName) {
                    val versionCode = it.getInt("versionCode")
                    result.add(Version(channelName, versionCode))
                }
            }
        } catch (e: JSONException) {
            throw JsonParseException(response, e)
        }
        return result
    }

    suspend fun getVersion(versionCode: Int): Version? {
        val response = try {
            client.get<String>("https://adodoz.cn/hzmgr/v1/changelogs.json")
        } catch (e: IOException) {
            throw NetworkException("获取更新日志失败", e)
        }
        try {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                val vc = it.getInt("versionCode")

                if (channel == channelName && vc == versionCode) {
                    return Version(channelName, versionCode)
                }
            }
        } catch (e: JSONException) {
            throw JsonParseException(response, e)
        }
        return null
    }

    suspend fun getVersion(versionName: String): Version? {
        val response = try {
            client.get<String>("https://adodoz.cn/hzmgr/v1/changelogs.json")
        } catch (e: IOException) {
            throw NetworkException("获取更新日志失败", e)
        }
        try {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                val vn = it.getString("versionName")

                if (channel == channelName && versionName == vn) {
                    val vc = it.getInt("versionCode")
                    return Version(channelName, vc)
                }
            }
        } catch (e: JSONException) {
            throw JsonParseException(response, e)
        }
        return null
    }
}