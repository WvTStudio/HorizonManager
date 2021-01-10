package org.wvt.horizonmgr.webapi.news

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Instant
import org.json.JSONArray
import org.json.JSONObject
import org.wvt.horizonmgr.webapi.forEachIndexed
import org.wvt.horizonmgr.webapi.parseJson

/**
 * 使用该类可以获取存储于 Adodoz 服务器上的新闻
 *
 * https://adodoz.cn/hzmgr/v1/news.json 存放着新闻的列表
 * https://adodoz.cn/hzmgr/v1/news/n.json 存放着 id 为 n 的新闻数据
 */
class MgrNewsModule {
    private val client = HttpClient(CIO)

    /**
     * 获取推荐新闻
     *
     * https://adodoz.cn/hzmgr/v1/news.json // 推荐新闻
     *
     * ```
     * [
     *   {
     *   "id": <int>, // 新闻 ID
     *   "cover": <str>, // 宣传封面
     *   "title": <str>, // 宣传标题
     *   "brief": <str>, // 宣传副标题
     *   "updateTime": <str> // 更新时间，格式：2020-12-25
     *   },
     *   ...
     * }
     * ```
     */
    suspend fun getNewsSuggestions(): List<NewsSuggestion> {
        val response = client.get<String>("https://adodoz.cn/hzmgr/v1/news.json")
        val result = mutableListOf<NewsSuggestion>()

        parseJson {
            val json = JSONArray(response)
            json.forEachIndexed<JSONObject> { index, item ->
                val id = item.getInt("id")
                val title = item.getString("title")
                val cover = item.getString("cover")
                val brief = item.getString("brief")
                val updateTime = item.getString("updateTime")

                result.add(NewsSuggestion(id, title, brief, cover, updateTime))
            }
        }

        return result
    }

    /**
     * {
     *   "id": 1,
     *   "title": <str>, // 新闻标题
     *   "brief": <str>, // 新闻摘要
     *   "coverUrl": <str>, // 封面 URL
     *   "content": <str> // 内容
     * }
     */
    suspend fun getNews(id: Int): News? {
        val response = client.get<HttpResponse>("https://adodoz.cn/hzmgr/v1/news/${id}.json")
        if (response.status == HttpStatusCode.OK) {
            parseJson {
                val json = JSONObject(response.readText())
                val newsId = json.getInt("id")
                val title = json.getString("title")
                val brief = json.getString("brief")
                val coverUrl = json.getString("coverUrl")
                val content = json.getString("content")
                return News(newsId, title, brief, coverUrl, content)
            }
        } else {
            return null
        }
    }
}

data class NewsSuggestion internal constructor(
    val newsId: Int,
    val cover: String,
    val title: String,
    val brief: String,
    private val updateISOTime: String
) {
    val updateTime = Instant.parse(updateISOTime)
}

data class News internal constructor(
    val id: Int,
    val title: String,
    val brief: String,
    val coverUrl: String,
    val content: String
)