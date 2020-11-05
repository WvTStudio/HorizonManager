package org.wvt.horizonmgr.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer

class NewsContentViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {
    data class NewsContent(
        val coverUrl: String,
        val title: String,
        val brief: String,
        val content: String
    )

    val newsContent = MutableStateFlow<NewsContent?>(null)

    fun refresh(newsId: Int) {
        viewModelScope.launch {
            val content = try {
                dependencies.webapi.getNewsContent(newsId)
            } catch (e: Exception) {
                // TODO: 2020/11/3 添加错误信息
                e.printStackTrace()
                return@launch
            }
            newsContent.value = content?.let {
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