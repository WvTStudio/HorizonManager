package org.wvt.horizonmgr.ui.news

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.webapi.NetworkException

private const val TAG = "NewsContentViewModel"

class NewsContentViewModel(
    dependencies: DependenciesContainer
) : ViewModel() {
    private val article = dependencies.article
    private var articleId: String = ""

    data class NewsContent(
        val title: String,
        val brief: String,
        val coverImage: String?,
        val content: String
    )

    sealed class Result {
        object Loading: Result()
        class Succeed(val value: NewsContent): Result()
        object NetworkError: Result()
        object NewsNotFound: Result()
        object OtherError: Result()
    }

    val content = MutableStateFlow<Result>(Result.Loading)

    fun load(articleId: String) {
        this.articleId = articleId
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            content.emit(Result.Loading)
            val result = try {
                article.getArticle(articleId)
            } catch (e: NetworkException) {
                content.emit(Result.NetworkError)
                Log.e(TAG, "获取新闻内容时出现网络错误", e)
                return@launch
            } catch (e: Exception) {
                content.emit(Result.OtherError)
                Log.e(TAG, "获取新闻内容时出现未知错误", e)
                return@launch
            }
            if (result == null) {
                content.emit(Result.NewsNotFound)
                return@launch
            }
            content.emit(Result.Succeed(
                NewsContent(
                    coverImage = result.coverImage,
                    title = result.title,
                    brief = result.brief,
                    content = result.content
                )
            ))
        }
    }
}