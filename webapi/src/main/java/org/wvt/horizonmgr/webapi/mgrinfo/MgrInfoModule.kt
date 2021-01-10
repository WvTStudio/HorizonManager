package org.wvt.horizonmgr.webapi.mgrinfo

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import org.json.JSONArray
import org.json.JSONObject
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.ParseException
import org.wvt.horizonmgr.webapi.forEach
import org.wvt.horizonmgr.webapi.parseJson


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
     * TODO-Warning 服务器的 v1 API 返回的 money 是 String
     * GET `请求 https://adodoz.cn/hzmgr/v1/donates.json` 获取捐赠列表
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
     * @throws [NetworkException], [ParseException]
     */
    suspend fun getDonateList(): List<DonateRecord> {
        val json = try {
            client.get<String>("https://adodoz.cn/hzmgr/v2/donates.json")
        } catch (e: Exception) {
            // TODO 检查异常的类型
            throw NetworkException("Ktor network exception", e)
        }

        val result = mutableListOf<DonateRecord>()

        parseJson {
            JSONArray(json).forEach<JSONObject> {
                val name = it.getString("name")
                val money = it.getInt("money")
                result.add(DonateRecord(name, money))
            }
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
        val json = client.get<String>("https://adodoz.cn/hzmgr/v1/qqgroups.json")
        val result = mutableListOf<QQGroup>()
        parseJson {
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
        }
        return result
    }

    /**
     * 获取所有版本通道
     */
    suspend fun getVersionChannels(): List<VersionChannel> {
        val response = client.get<String>("https://adodoz.cn/hzmgr/v1/versioninfo.json")
        val result = mutableListOf<VersionChannel>()

        parseJson {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                result.add(VersionChannel(channel))
            }
        }
        return result
    }

    /**
     * 获取名为 [channelName] 的版本通道
     */
    suspend fun getChannelByName(channelName: String): VersionChannel? {
        val response = client.get<String>("https://adodoz.cn/hzmgr/v1/versioninfo.json")

        parseJson {
            JSONArray(response).forEach<JSONObject> {
                val channel = it.getString("channel")
                if (channel == channelName) {
                    return VersionChannel(channel)
                }
            }
        }

        return null
    }
}