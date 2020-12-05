package org.wvt.horizonmgr.webapi.mgrmodule

import io.ktor.client.*
import io.ktor.client.engine.cio.*
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
internal class MgrModuleAdodozV1 : MgrModule {
    private val client = HttpClient(CIO)

    internal data class DonateRecordImpl(
        internal val donorName: String,
        internal val money: Int
    ) : DonateRecord {
        override fun money() = money
        override fun donorName() = donorName
    }

    internal data class QQGroupImpl(
        internal val name: String,
        internal val description: String,
        internal val avatarUrl: String,
        internal val urlLink: String,
        internal val status: String
    ) : QQGroup {
        override fun getDescription() = description
        override fun getAvatarUrl() = avatarUrl
        override fun getName() = name
        override fun getUrlLink() = urlLink
        override fun getStatus() = status
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
    override suspend fun getDonateList(): List<DonateRecord> {
        val json = try {
            client.get<String>("https://adodoz.cn/hzmgr/v1/donates.json")
        } catch (e: Exception) {
            // TODO 检查异常的类型
            throw NetworkException("Ktor network exception", e)
        }

        val result = mutableListOf<DonateRecord>()

        parseJson {
            JSONArray(json).forEach<JSONObject> {
                val name = it.getString("name")
                val money = it.getInt("money")
                result.add(DonateRecordImpl(name, money))
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
    override suspend fun getQQGroupList(): List<QQGroupImpl> {
        val json = client.get<String>("https://adodoz.cn/hzmgr/v1/qqgroups.json")
        val result = mutableListOf<QQGroupImpl>()
        parseJson {
            JSONArray(json).forEach<JSONObject> {
                result.add(
                    QQGroupImpl(
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
}