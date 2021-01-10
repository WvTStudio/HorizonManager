package org.wvt.horizonmgr.webapi.mgrinfo

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.get
import io.ktor.client.request.*
import org.json.JSONArray
import org.json.JSONObject
import org.wvt.horizonmgr.webapi.forEach
import org.wvt.horizonmgr.webapi.parseJson

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
        val response = client.get<String>("https://adodoz.cn/hzmgr/v1/versioninfo.json")

        parseJson {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                if (channel == channelName) {
                    val versionCode = it.getInt("latestVersionCode")
                    return Version(channelName, versionCode)
                }
            }
        }

        error("Server returned the data, but there was no latest version data entry.")
    }

    suspend fun getVersions(): List<Version> {
        val response = client.get<String>("https://adodoz.cn/hzmgr/v1/changelogs.json")
        val result = mutableListOf<Version>()
        parseJson {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                if (channel == channelName) {
                    val versionCode = it.getInt("versionCode")
                    result.add(Version(channelName, versionCode))
                }
            }
        }
        return result
    }

    suspend fun getVersion(versionCode: Int): Version? {
        val response = client.get<String>("https://adodoz.cn/hzmgr/v1/changelogs.json")
        parseJson {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                val vc = it.getInt("versionCode")

                if (channel == channelName && vc == versionCode) {
                    return Version(channelName, versionCode)
                }
            }
        }
        return null
    }

    suspend fun getVersion(versionName: String): Version? {
        val response = client.get<String>("https://adodoz.cn/hzmgr/v1/changelogs.json")
        parseJson {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                val vn = it.getString("versionName")

                if (channel == channelName && versionName == vn) {
                    val vc = it.getInt("versionCode")
                    return Version(channelName, vc)
                }
            }
        }
        return null
    }
}