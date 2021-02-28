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
 * 与 Adodoz 服务器通信，使用服务器的 V1 API
 */
class MgrInfoModule {
    private val client = HttpClient(CIO) {
        HttpResponseValidator {
            validateResponse { }
        }
    }

    /**
     * GET `请求 https://adodoz.cn/hzmgr/v2/donates.json` 获取捐赠列表
     * 服务器返回如下的 Json 数据
     *
     * ```json
     * [
     *   {
     *     "name": <str>,
     *     "money": <int> // 单位：分
     *   }, ...
     * ]
     * ```
     *
     * @throws [NetworkException], [JsonParseException]
     */
    suspend fun getDonateList(): List<DonateRecord> {
        val json = try {
            client.get<String>("https://adodoz.cn/hzmgr/v2/donates.json")
        } catch (e: IOException) {
            throw NetworkException("获取捐赠列表失败", e)
        }

        val result = mutableListOf<DonateRecord>()

        try {
            JSONArray(json).forEach<JSONObject> {
                val name = it.getString("name")
                val money = it.getInt("money")
                result.add(DonateRecord(name, money))
            }
        } catch (e: JSONException) {
            throw JsonParseException(json, e)
        }

        return result
    }

    /**
     * GET 请求 `https://adodoz.cn/hzmgr/v1/qqgroups.json` 获取可加入的群组
     * 服务器返回如下的 Json 数据
     *
     * ```json
     * [
     *   {
     *     "name": <str>,
     *     "description": <str>,
     *     "avatar": <str>,
     *     "status": <str>,
     *     "url": <str>
     *   }, ...
     * ]
     * ```
     */
    suspend fun getQQGroupList(): List<QQGroup> {
        val json = try {
            client.get<String>("https://adodoz.cn/hzmgr/v1/qqgroups.json")
        } catch (e: IOException) {
            throw NetworkException("获取群组列表失败", e)
        }
        val result = mutableListOf<QQGroup>()
        try {
            JSONArray(json).forEach<JSONObject> {
                result.add(
                    QQGroup(
                        name = it.getString("name"),
                        description = it.getString("description"),
                        avatarUrl = it.getString("avatar"),
                        urlLink = it.getString("url"),
                        status = it.getString("status")
                    )
                )
            }
        } catch (e: JSONException) {
            throw JsonParseException(json, e)
        }
        return result
    }

    /**
     * 获取所有版本通道
     */
    suspend fun getVersionChannels(): List<VersionChannel> {
        val response = try {
            client.get<String>("https://adodoz.cn/hzmgr/v1/versioninfo.json")
        } catch (e: IOException) {
            throw NetworkException("获取版本信息失败", e)
        }
        val result = mutableListOf<VersionChannel>()

        try {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                result.add(VersionChannel(channel))
            }
        } catch (e: JSONException) {
            throw JsonParseException(response, e)
        }
        return result
    }

    /**
     * 获取名为 [channelName] 的版本通道
     */
    suspend fun getChannelByName(channelName: String): VersionChannel? {
        val response = try {
            client.get<String>("https://adodoz.cn/hzmgr/v1/versioninfo.json")
        } catch (e: IOException) {
            throw NetworkException("获取版本信息失败", e)
        }

        try {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                if (channel == channelName) {
                    return VersionChannel(channel)
                }
            }
        } catch (e: JSONException) {
            throw JsonParseException(response, e)
        }

        return null
    }
}