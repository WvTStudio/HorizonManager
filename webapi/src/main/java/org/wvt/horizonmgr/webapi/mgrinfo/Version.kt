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
import org.wvt.horizonmgr.webapi.ServerException
import org.wvt.horizonmgr.webapi.forEach
import java.io.IOException

/**
 * 代表某个版本号
 * 通过此类可以获取某个版本号的 channel，更新日志等
 */
class Version internal constructor(
    val channelName: String,
    val versionCode: Int
) {
    private val client = HttpClient(CIO) {
        HttpResponseValidator { validateResponse { } }
    }

    suspend fun getData(): VersionData {
        val response = try {
            client.get<String>("https://adodoz.cn/hzmgr/v1/changelogs.json")
        } catch (e: IOException) {
            throw NetworkException("获取更新日志失败", e)
        }
        var result: VersionData? = null
        try {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                val versionCode = it.getInt("versionCode")
                if (channel == channelName && versionCode == this.versionCode) {
                    val versionName = it.getString("versionName")
                    val changelog = it.getString("changelog")
                    result = VersionData(channelName, versionCode, versionName, changelog)
                }
            }
        } catch (e: JSONException) {
            throw JsonParseException(response, e)
        }
        return result ?: throw ServerException("无法找到更新日志")
    }
}