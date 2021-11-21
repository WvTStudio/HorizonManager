package org.wvt.horizonmgr.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.news.MgrArticleModule
import javax.inject.Inject

private const val TAG = "ArticleContentViewModel"

@HiltViewModel
class ArticleContentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val articleModule: MgrArticleModule
) : ViewModel() {
    private var articleId: String = savedStateHandle.get<String>("id")!!

    init { refresh() }

    data class ArticleContent(
        val title: String,
        val brief: String,
        val coverImage: String?,
        val content: String
    )

    sealed class Result {
        object Loading : Result()
        class Succeed(val value: ArticleContent) : Result()
        object NetworkError : Result()
        object ArticleNotFound : Result()
        object OtherError : Result()
    }

    val isRefreshing = MutableStateFlow(false)
    val content = MutableStateFlow<Result>(Result.Loading)

    fun load(articleId: String) {
        if (articleId != this.articleId) {
            this.articleId = articleId
            viewModelScope.launch(Dispatchers.IO) {
                content.emit(Result.Loading)
                loadData()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            isRefreshing.value = true
            loadData()
            isRefreshing.value = false
        }
    }

    private suspend fun loadData() {
        val result = try {
            articleModule.getArticle(articleId)
        } catch (e: NetworkException) {
            content.emit(Result.NetworkError)
            Log.e(TAG, "获取文章内容时出现网络错误", e)
            return
        } catch (e: Exception) {
            content.emit(Result.OtherError)
            Log.e(TAG, "获取文章内容时出现未知错误", e)
            return
        }
        if (result == null) {
            content.emit(Result.ArticleNotFound)
            return
        }
        content.emit(
            Result.Succeed(
                ArticleContent(
                    coverImage = result.coverImage,
                    title = result.title,
                    brief = result.brief,
                    content = result.content
                )
            )
        )
    }
}