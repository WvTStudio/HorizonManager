package org.wvt.horizonmgr.ui.modulemanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.hzpack.InstalledPackage
import org.wvt.horizonmgr.service.level.LevelInfo
import org.wvt.horizonmgr.service.level.MCLevel

private const val TAG = "ICLevelTabVM"

class ICLevelTabViewModel(dependencies: DependenciesContainer) : ViewModel() {
    private val manager = dependencies.manager
    private val levelTransporter = dependencies.levelTransporter

    sealed class State {
        object Loading : State()
        object PackageNotSelected : State()
        object OK : State()
        class Error(val e: Throwable?) : State()
    }

    val state = MutableStateFlow<State>(State.Loading)

    val levels = MutableStateFlow<List<LevelInfo>>(emptyList())
    private var cachedLevels = emptyMap<LevelInfo, MCLevel>()

    private var pack: InstalledPackage? = null

    fun setPackage(uuid: String?) {
        Log.d(TAG, "New package: $uuid")
        viewModelScope.launch {
            if (uuid == null) {
                state.emit(State.PackageNotSelected)
            } else {
                withContext(Dispatchers.IO) {
                    pack = manager.getInstalledPackages().find {
                        it.getInstallationInfo().internalId == uuid
                    }
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            pack?.let { pack ->
                state.emit(State.Loading)
                val result = try {
                    pack.getLevels()
                } catch (e: Exception) {
                    // TODO 显示错误信息
                    Log.e(TAG, "获取 IC 存档失败", e)
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
                        state.emit(State.Error(e))
                        return@launch
                    }
                }.toMap()
                cachedLevels = mapped
                levels.emit(mapped.keys.toList())
                state.emit(State.OK)
            }
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

    suspend fun copyToMC(level: LevelInfo) {
        withContext(Dispatchers.IO){
            cachedLevels[level]?.let {
                levelTransporter.copyToMC(it)
            }
        }
    }

    suspend fun moveToMC(level: LevelInfo) {
        withContext(Dispatchers.IO){
            cachedLevels[level]?.let {
                levelTransporter.moveToMC(it)
            }
        }
    }
}

