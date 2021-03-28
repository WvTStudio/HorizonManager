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
import org.wvt.horizonmgr.service.level.ZipMCLevel
import org.wvt.horizonmgr.ui.components.InputDialogHostState
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import java.io.File

private const val TAG = "ICLevelTabVM"

class ICLevelTabViewModel(dependencies: DependenciesContainer) : ViewModel() {
    private val manager = dependencies.manager
    private val levelTransporter = dependencies.levelTransporter

    sealed class State {
        object Loading : State()
        object PackageNotSelected : State()
        object OK : State()
        class Error(val message: String) : State()
    }

    val state = MutableStateFlow<State>(State.Loading)

    val levels = MutableStateFlow<List<LevelInfo>>(emptyList())
    val errors = MutableStateFlow<List<String>>(emptyList())

    private var cachedLevels = emptyMap<LevelInfo, MCLevel>()

    private var pack: InstalledPackage? = null

    val progressState = MutableStateFlow<ProgressDialogState?>(null)
    val inputDialogState = InputDialogHostState()

    fun setPackage(uuid: String?) {
        Log.d(TAG, "New package: $uuid")
        viewModelScope.launch {
            if (uuid == null) {
                state.emit(State.PackageNotSelected)
            } else {
                pack = manager.getInstalledPackage(uuid)
                if (pack == null) {
                    state.emit(State.Error("您选择的分包可能已被移动或删除"))
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            pack?.let { pack ->
                state.emit(State.Loading)
                val result = try {
                    pack.getLevelManager().getLevels()
                } catch (e: Exception) {
                    // TODO 显示错误信息
                    Log.e(TAG, "获取 IC 存档失败", e)
                    state.emit(State.Error("获取存档失败"))
                    return@launch
                }
                val mappedErrors = result.errors.map {
                    "${it.file.absolutePath}: ${it.error.message ?: "未知错误"}"
                }
                val mapped = mutableMapOf<LevelInfo, MCLevel>().apply {
                    try {
                        result.levels.forEach {
                            put(it.getInfo(), it)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "获取存档信息失败", e)
                        // TODO: 2021/3/3 显示错误信息
                        state.emit(State.Error("获取存档信息失败"))
                        return@launch
                    }
                }.toMap()
                cachedLevels = mapped
                errors.emit(mappedErrors)
                levels.emit(mapped.keys.toList())
                state.emit(State.OK)
            }
        }
    }

    fun deleteLevel(item: LevelInfo) {
        viewModelScope.launch {
            progressState.emit(ProgressDialogState.Loading("正在删除"))
            try {
                mDeleteLevel(item)
            } catch (e: Exception) {
                progressState.emit(
                    ProgressDialogState.Failed(
                        "删除失败",
                        e.localizedMessage ?: ""
                    )
                )
                return@launch
            }
            progressState.emit(ProgressDialogState.Finished("删除完成"))
            load()
        }
    }

    private suspend fun mDeleteLevel(level: LevelInfo) {
        withContext(Dispatchers.IO) {
            cachedLevels[level]?.delete()
        }
    }

    fun renameLevel(item: LevelInfo) {
        viewModelScope.launch {
            val result: InputDialogHostState.DialogResult =
                inputDialogState.showDialog(
                    "New-${item.name}",
                    "请输入新名称",
                    "新名称"
                )
            if (result is InputDialogHostState.DialogResult.Confirm) {
                progressState.emit(ProgressDialogState.Loading("正在重命名"))
                try {
                    mRenameLevel(item, result.input)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to rename level", e)
                    progressState.emit(
                        ProgressDialogState.Failed(
                            "重命名失败",
                            e.localizedMessage ?: ""
                        )
                    )
                    return@launch
                }
                progressState.emit(ProgressDialogState.Finished("重命名成功"))
                load()
            }
        }
    }

    private suspend fun mRenameLevel(level: LevelInfo, newName: String) {
        withContext(Dispatchers.IO) {
            cachedLevels[level]?.rename(newName)
        }
    }

    fun copy(item: LevelInfo) {
        viewModelScope.launch {
            progressState.emit(ProgressDialogState.Loading("正在复制存档到 MC"))
            try {
                copyToMC(item)
            } catch (e: Exception) {
                progressState.emit(ProgressDialogState.Failed("复制失败", e.message ?: "未知错误"))
                return@launch
            }
            progressState.emit(ProgressDialogState.Finished("复制成功"))
        }
    }

    private suspend fun copyToMC(level: LevelInfo) {
        withContext(Dispatchers.IO) {
            cachedLevels[level]?.let {
                levelTransporter.copyToMC(it)
            }
        }
    }

    fun move(item: LevelInfo) {
        viewModelScope.launch {
            progressState.emit(ProgressDialogState.Loading("正在移动存档到 MC"))
            try {
                moveToMC(item)
            } catch (e: Exception) {
                progressState.emit(ProgressDialogState.Failed("移动失败", e.message ?: "未知错误"))
                return@launch
            }
            progressState.emit(ProgressDialogState.Finished("移动成功"))
            load()
        }
    }

    private suspend fun moveToMC(level: LevelInfo) {
        withContext(Dispatchers.IO) {
            cachedLevels[level]?.let {
                levelTransporter.moveToMC(it)
            }
        }
    }

    fun selectedFileToInstall(path: String) {
        viewModelScope.launch {
            val pack = pack ?: return@launch
            val levelManager = pack.getLevelManager()

            try {
                progressState.emit(ProgressDialogState.Loading("正在解析"))
                val file = File(path)
                val level = try {
                    ZipMCLevel.parse(file)
                } catch (e: ZipMCLevel.NotZipMCLevelException) {
                    progressState.emit(ProgressDialogState.Failed("解析失败", "您选择的文件可能不是一个正确的存档"))
                    return@launch
                }
                progressState.emit(ProgressDialogState.Loading("正在安装"))
                val task = levelManager.installLevel(level)
                // TODO: 2021/3/21 安装进度
                try {
                    task.await()
                } catch (e: Exception) {
                    progressState.emit(ProgressDialogState.Failed("安装失败", "安装时出现错误", e.message))
                }
            } catch (e: Exception) {
                progressState.emit(ProgressDialogState.Failed("安装失败", "出现未知错误", e.message))
            }
        }
    }

    fun dismissProgressDialog() {
        viewModelScope.launch {
            progressState.emit(null)
        }
    }
}

