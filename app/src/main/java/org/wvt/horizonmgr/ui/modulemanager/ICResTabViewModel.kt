package org.wvt.horizonmgr.ui.modulemanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.respack.ResourcePackManager
import org.wvt.horizonmgr.service.respack.ResourcePackManifest
import org.wvt.horizonmgr.service.respack.ZipResourcePackage
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import java.io.File

private const val TAG = "ICResTabVM"

class ICResTabViewModel(dependencies: DependenciesContainer) : ViewModel() {
    private val packMgr = dependencies.manager
    private val localCache = dependencies.localCache

    val state = MutableStateFlow<State>(State.Loading)
    val resPacks = MutableStateFlow<List<ResPack>>(emptyList())
    val progressState = MutableStateFlow<ProgressDialogState?>(null)

    private var resPackManager: ResourcePackManager? = null

    sealed class State {
        object Done : State()
        object Loading : State()
        class Error(val message: String) : State()
    }

    data class ResPack(
        val iconPath: String?,
        val manifest: ResourcePackManifest
    )

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            state.emit(State.Loading)
            val selectedUUID = localCache.getSelectedPackageUUID()
            if (selectedUUID == null) {
                state.emit(State.Error("您还未选择分包"))
                return@launch
            }
            val selectedPack = packMgr.getInstalledPackages()
                .find { it.getInstallationInfo().internalId == selectedUUID }
            if (selectedPack == null) {
                state.emit(State.Error("您选择的分包可能已被移动或删除"))
                return@launch
            }

            val resManager = selectedPack.getResManager()
            resPackManager = resManager

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
                state.emit(State.Error("获取资源包失败"))
                return@launch
            }

            resPacks.emit(result)
            state.emit(State.Done)
        }
    }

    fun selectedFileToInstall(path: String) {
        viewModelScope.launch {
            val resPackManager = resPackManager
            if (resPackManager == null) {
                progressState.emit(ProgressDialogState.Failed("安装失败", "您还没有选择分包"))
                return@launch
            }

            try {
                progressState.emit(ProgressDialogState.Loading("正在解析"))
                val file = File(path)
                val respack = try {
                    ZipResourcePackage.parse(file)
                } catch (e: ZipResourcePackage.NotZipResPackException) {
                    progressState.emit(ProgressDialogState.Failed("解析失败", "您选择的文件可能不是一个正确的资源包"))
                    return@launch
                }
                progressState.emit(ProgressDialogState.Loading("正在安装"))
                val task = resPackManager.install(respack)
                val progressJob = launch {
                    task.progressChannel().consumeAsFlow().conflate().collect {
                        progressState.emit(ProgressDialogState.ProgressLoading("正在安装", it))
                        delay(500)
                    }
                }
                try {
                    task.await()
                } catch (e: Exception) {
                    progressState.emit(ProgressDialogState.Failed("安装失败", "安装时出现错误", e.message))
                } finally {
                    progressJob.cancel()
                }
                load()
                progressState.emit(ProgressDialogState.Finished("安装完成"))
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