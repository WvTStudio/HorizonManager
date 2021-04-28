package org.wvt.horizonmgr.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import java.text.SimpleDateFormat

class HomeViewModel(
    dependencies: DependenciesContainer
) : ViewModel() {
    companion object {
        const val TAG = "NewsViewModel"
    }

    private val articleModule = dependencies.article

    sealed class ContentResource {
        data class Article(
            val id: String,
            val title: String,
            val brief: String,
            val coverUrl: String?,
            val updateTime: String
        ) : ContentResource()
    }

    sealed class State {
        object Loading : State()
        object Succeed : State()
        data class Error(val e: Throwable) : State()
    }

    private var initiate = false

    val contentResources = MutableStateFlow(emptyList<ContentResource>())

    val state: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    val isRefreshing = MutableStateFlow(false)

    private val formatter = SimpleDateFormat.getDateInstance()

    fun load() {
        if (!initiate) {
            initiate = true
            state.value = State.Loading
            viewModelScope.launch(Dispatchers.IO) {
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
            articleModule.getRecommendedArticle().map {
                ContentResource.Article(
                    it.id,
                    it.title,
                    it.brief,
                    it.coverImage,
                    formatter.format(it.updateTimeInstant.epochSeconds)
                )
            }
        } catch (e: Exception) {
            state.emit(State.Error(e))
            Log.e(TAG, "Failed to refresh content resources", e)
            return
        }
        contentResources.emit(result)
        state.emit(State.Succeed)
    }
}