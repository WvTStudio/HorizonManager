package org.wvt.horizonmgr.ui.modulemanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.level.LevelInfo
import org.wvt.horizonmgr.service.level.MCLevel

private const val TAG = "MCLevelTabVM"

class MCLevelTabViewModel(dependencies: DependenciesContainer) : ViewModel() {
    private val levelManager = dependencies.mcLevelManager

    val levels = MutableStateFlow<List<LevelInfo>>(emptyList())
    private var cachedLevels = emptyMap<LevelInfo, MCLevel>()

    fun load() {
        viewModelScope.launch {
            val result = try {
                levelManager.getLevels()
            } catch (e: Exception) {
                // TODO 显示错误信息
                Log.e(TAG, "获取存档失败", e)
                return@launch
            }
            val mapped = mutableMapOf<LevelInfo, MCLevel>().apply {
                try {
                    result.forEach {
                        put(it.getInfo(), it)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "获取存档信息失败", e)
                    // TODO: 2021/3/3 显示错误信息
                    return@launch
                }
            }.toMap()
            cachedLevels = mapped
            levels.emit(mapped.keys.toList())
        }
    }

    suspend fun deleteLevel(level: LevelInfo) {
        withContext(Dispatchers.IO) {
            cachedLevels[level]?.delete()
        }
    }

    suspend fun renameLevel(level: LevelInfo, newName: String) {
        withContext(Dispatchers.IO) {
            cachedLevels[level]?.rename(newName)
        }
    }
}