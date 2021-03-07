package org.wvt.horizonmgr.ui.modulemanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.respack.ResourcePackManifest
import java.io.File

private const val TAG = "MCResTabVM"

class MCResTabViewModel(dependencies: DependenciesContainer) : ViewModel() {
    private val resManager = dependencies.mcResourcePackManager

    val resPacks = MutableStateFlow<List<ResPack>>(emptyList())

    val state = MutableStateFlow<State>(State.LOADING)

    enum class State {
        FINISHED, LOADING, FAILED
    }

    data class ResPack(
        val iconPath: String?,
        val manifest: ResourcePackManifest
    )

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            state.emit(State.LOADING)
            val errors = mutableListOf<Pair<File, Exception>>()
            val result = mutableListOf<ResPack>()

            try {
                resManager.getPackages().forEach {
                    try {
                        result.add(ResPack(it.getIcon()?.absolutePath, it.getManifest()))
                    } catch (e: Exception) {
                        errors.add(it.directory to e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取资源包失败", e)
                state.emit(State.FAILED)
                return@launch
            }
            resPacks.emit(result)
            state.emit(State.FINISHED)
        }
    }
}