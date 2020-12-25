package org.wvt.horizonmgr.ui.modulemanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.legacyservice.HorizonManager

class MCLevelTabViewModel(
    dependenciesContainer: DependenciesContainer
) : ViewModel() {
    private val hzmgr = dependenciesContainer.horizonManager

    val levels = MutableStateFlow<List<HorizonManager.LevelInfo>>(emptyList())

    fun load() {
        viewModelScope.launch {
            try {
                levels.value = hzmgr.getMCLevels()
            } catch (e: Exception) {
                // TODO 显示错误信息
                e.printStackTrace()
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