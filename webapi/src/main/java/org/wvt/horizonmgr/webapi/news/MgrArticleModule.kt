package org.wvt.horizonmgr.webapi.news

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.wvt.horizonmgr.webapi.JsonParseException
import org.wvt.horizonmgr.webapi.NetworkException
import java.io.IOException

class MgrArticleModule {
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    /**
     * https://adodoz.cn/hzmgr/v2/news_suggestions.json
     * ```
     * [
     *   {
     *   "id": <str>, // 新闻 ID
     *   "coverImage": <str>, // 宣传封面
     *   "title": <str>, // 宣传标题
     *   "brief": <str>, // 宣传副标题
     *   "updateTime": <str> // 更新时间，格式：2020-12-25
     *   },
     *   ...
     * }
     */
    @Throws(NetworkException::class, JsonParseException::class)
    suspend fun getRecommendedArticle(): List<RecommendedArticle> {
        return try {
            client.get<List<RecommendedArticle>>("https://adodoz.cn/hzmgr/v2/recommended_articles.json")
        } catch (e: IOException) {
            throw NetworkException("获取推荐资讯失败", e)
        } catch (e: SerializationException) {
            throw JsonParseException("unknown", e)
        }
    }

    /**
     * 获取文章信息
     * https://adodoz.cn/hzmgr/v2/article/info/${newsId}.json
     *
     * 获取文章内容
     * https://adodoz.cn/hzmgr/v2/article/content/${newsId}.json
     */
    @Throws(NetworkException::class, ClientRequestException::class)
    suspend fun getArticle(newsId: String): Article? {
        val brief = try {
            client.get<ArticleBrief>("https://adodoz.cn/hzmgr/v2/article/info/${newsId}.json")
        } catch (e: IOException) {
            throw NetworkException("获取文章 $newsId 信息失败", e)
        } catch (e: SerializationException) {
            throw JsonParseException("unknown", e)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) return null
            else throw e
        }
        val content = try {
            client.get<String>("https://adodoz.cn/hzmgr/v2/article/content/${newsId}.md")
        } catch (e: IOException) {
            throw NetworkException("获取文章 $newsId 内容失败", e)
        }
        return Article(newsId, brief.title, brief.brief, brief.coverImage, content)
    }

    @Serializable
    data class ArticleBrief(
        val title: String,
        val coverImage: String?,
        val brief: String
    )

    @Serializable
    data class RecommendedArticle internal constructor(
        val id: String,
        val coverImage: String?,
        val title: String,
        val brief: String,
        val updateTime: String
    ) {
        @Contextual
        val updateTimeInstant = Instant.parse(updateTime)
    }

    data class Article internal constructor(
        val id: String,
        val title: String,
        val brief: String,
        val coverImage: String?,
        val content: String
    )
}

