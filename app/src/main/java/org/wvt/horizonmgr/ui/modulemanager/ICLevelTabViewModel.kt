package org.wvt.horizonmgr.ui.modulemanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.legacyservice.HorizonManager

class ICLevelTabViewModel(
    dependenciesContainer: DependenciesContainer
) : ViewModel() {
    sealed class State {
        object Loading : State()
        object PackageNotSelected : State()
        object OK : State()
    }

    val state = MutableStateFlow<State>(State.Loading)

    private val hzmgr = dependenciesContainer.horizonManager

    val levels = MutableStateFlow<List<HorizonManager.LevelInfo>>(emptyList())

    private var packageUUID: String? = null

    fun setPackage(uuid: String?) {
        Log.d("ICLevelTabVM", "new package: $uuid")
        if (uuid == null) {
            state.value = State.PackageNotSelected
        }
        packageUUID = uuid
    }

    fun getPackage(): String? {
        return packageUUID
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            packageUUID?.let {
                state.value = State.Loading
                try {
                    levels.value = hzmgr.getICLevels(it)
                } catch (e: Exception) {
                    // TODO 显示错误信息
                    e.printStackTrace()
                }
                state.value = State.OK
            } ?: run {
                levels.value = emptyList()
            }
        }
    }

    suspend fun deleteLevel(path: String) {
        hzmgr.deleteLevelByPath(path)
    }

    suspend fun renameLevel(path: String, newname: String) {
        hzmgr.renameLevelNameByPath(path, newname)
    }
}

