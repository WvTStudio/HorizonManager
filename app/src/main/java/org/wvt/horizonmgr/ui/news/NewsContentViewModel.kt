package org.wvt.horizonmgr.ui.news

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer

private const val TAG = "NewsContentViewModel"

class NewsContentViewModel(
    dependencies: DependenciesContainer
) : ViewModel() {
    private val news = dependencies.news

    private var newsId: Int = -1

    data class NewsContent(
        val coverUrl: String,
        val title: String,
        val brief: String,
        val content: String
    )

    sealed class Result {
        object Loading: Result()
        class Failure(val e: Throwable): Result()
        class Succeed(val value: NewsContent): Result()
    }

    val content: MutableState<Result> = mutableStateOf(Result.Loading)

    val newsContent = MutableStateFlow<NewsContent?>(null)

    class NetworkException(override val cause: Throwable) : Exception()
    class NewsNotFoundException() : Exception()

    fun load(newsId: Int) {
        this.newsId = newsId
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            content.value = Result.Loading
            val result = try {
                news.getNews(newsId)
            } catch (e: NetworkException) {
                content.value = Result.Failure(NewsContentViewModel.NetworkException(e))
                // TODO: 2021/2/8 添加网络错误信息
                return@launch
            } catch (e: Exception) {
                content.value = Result.Failure(e)
                Log.e(TAG, "获取新闻内容失败", e)
                // TODO: 2021/2/8 添加错误信息
                return@launch
            }
            if (result == null) {
                content.value = Result.Failure(NewsNotFoundException())
                return@launch
            }
            content.value = Result.Succeed(
                NewsContent(
                    coverUrl = result.coverUrl,
                    title = result.title,
                    brief = result.brief,
                    content = result.content
                )
            )

            newsContent.value = result?.let {
                NewsContent(
                    coverUrl = it.coverUrl,
                    title = it.title,
                    brief = it.brief,
                    content = it.content
                )
            }
        }
    }
}