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
        val response = client.get<String>("https://adodoz.cn/hzmgr/v1/changelogs.json")
        var result: VersionData? = null
        parseJson {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                val versionCode = it.getInt("versionCode")
                if (channel == channelName && versionCode == this.versionCode) {
                    val versionName = it.getString("versionName")
                    val changelog = it.getString("changelog")
                    result = VersionData(channelName, versionCode, versionName, changelog)
                    return@parseJson
                }
            }
        }
        return result ?: error("Data seemed to be removed on the server.")
    }
}