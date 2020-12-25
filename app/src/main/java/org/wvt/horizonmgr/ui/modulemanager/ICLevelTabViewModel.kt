package org.wvt.horizonmgr.ui.modulemanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.legacyservice.HorizonManager

class ICLevelTabViewModel(
    dependenciesContainer: DependenciesContainer
) : ViewModel() {
    private val hzmgr = dependenciesContainer.horizonManager

    val levels = MutableStateFlow<List<HorizonManager.LevelInfo>>(emptyList())

    private var packageUUID: String? = null

    fun setPackage(uuid: String?) {
        packageUUID = uuid
    }

    fun getPackage(): String? {
        return packageUUID
    }

    fun load() {
        viewModelScope.launch {
            packageUUID?.let {
                try {
                    levels.value = hzmgr.getICLevels(it)
                } catch (e: Exception) {
                    // TODO 显示错误信息
                    e.printStackTrace()
                }
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

