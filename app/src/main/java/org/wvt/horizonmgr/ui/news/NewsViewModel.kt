package org.wvt.horizonmgr.ui.news

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.wvt.horizonmgr.DependenciesContainer
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.*

class NewsViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {
    companion object {
        const val TAG = "NewsViewModel"
    }

    sealed class News {
        data class Article(
            val id: Int,
            val title: String,
            val brief: String,
            val coverUrl: String?,
            val updateTime: String
        ) : News()
    }

    sealed class State {
        object Loading : State()
        object Succeed : State()
        data class Error(val e: Throwable) : State()
    }

    val news = MutableStateFlow(emptyList<News>())

    val state: MutableStateFlow<State> = MutableStateFlow(State.Loading)

    private val formatter = SimpleDateFormat.getDateInstance()

    fun refresh() {
        state.value = State.Loading

        viewModelScope.launch {
            val result = try {
                dependencies.news.getNewsSuggestions().map {
                    News.Article(
                        it.newsId,
                        it.title,
                        it.brief,
                        it.cover.takeIf { it.isNotBlank() },
                        formatter.format(Date(it.updateTime.epochSeconds))
                    )
                }
            } catch (e: Exception) {
                state.value = State.Error(e)
                Log.e(TAG, "Failed to refresh news", e)
                e.printStackTrace()
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