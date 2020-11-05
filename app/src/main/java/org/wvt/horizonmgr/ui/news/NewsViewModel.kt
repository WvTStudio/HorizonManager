package org.wvt.horizonmgr.ui.news

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer

class NewsViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {
    sealed class News {
        data class Article(
            val id: Int,
            val title: String,
            val brief: String,
            val coverUrl: String
        ) : News()
    }

    sealed class State {
        object Loading : State()
        object Succeed : State()
        data class Error(val e: Throwable) : State()
    }

    val news = MutableStateFlow(emptyList<News>())

    val state: MutableStateFlow<State> = MutableStateFlow(State.Loading)

    fun refresh() {
        state.value = State.Loading

        viewModelScope.launch {
            val result = try {
                dependencies.webapi.getNews().map { (id, title, brief, coverUrl) ->
                    News.Article(id, title, brief, coverUrl)
                }
            } catch (e: Exception) {
                state.value = State.Error(e)
                return@launch
            }
            news.value = result
            state.value = State.Succeed
        }
    }

    fun newsDetail(context: Context, id: Int) {
        context.startActivity(Intent(context, NewsContentActivity::class.java).also {
            it.putExtra("id", id)
        })
    }
}