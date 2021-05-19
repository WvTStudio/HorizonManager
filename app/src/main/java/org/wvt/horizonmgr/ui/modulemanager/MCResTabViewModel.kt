package org.wvt.horizonmgr.ui.modulemanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.respack.ResourcePackManifest
import org.wvt.horizonmgr.service.respack.ZipResourcePackage
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import java.io.File
import javax.inject.Inject

private const val TAG = "MCResTabVM"

@HiltViewModel
class MCResTabViewModel @Inject constructor(dependencies: DependenciesContainer) : ViewModel() {
    private val resManager = dependencies.mcResourcePackManager

    private var initialized = false

    val resPacks = MutableStateFlow<List<ResPack>>(emptyList())
    val errors = MutableStateFlow<List<String>>(emptyList())
    val state = MutableStateFlow<State>(State.Loading)
    val progressState = MutableStateFlow<ProgressDialogState?>(null)
    val isRefreshing = MutableStateFlow(false)

    sealed class State {
        object Loading : State()
        object Done : State()
        class Error(val message: String) : State()
    }

    data class ResPack(
        val iconPath: String?,
        val manifest: ResourcePackManifest
    )

    fun refresh() {
        if (!initialized) viewModelScope.launch(Dispatchers.IO) {
            initialized = true
            state.emit(State.Loading)
            loadData()
        } else viewModelScope.launch(Dispatchers.IO) {
            isRefreshing.emit(true)
            loadData()
            isRefreshing.emit(false)
        }
    }

    private suspend fun loadData() {
        val result = try {
            resManager.getPackages()
        } catch (e: Exception) {
            Log.e(TAG, "获取资源包失败", e)
            state.emit(State.Error("获取资源包失败"))
            return
        }
        val mappedErrors = result.errors.map {
            "${it.file.absolutePath}: ${it.error.message ?: "未知错误"}"
        }
        val packs = result.resPacks.map {
            try {
                ResPack(it.getIcon()?.absolutePath, it.getManifest())
            } catch (e: Exception) {
                // 这一步只有可能出现在解析过程中文件被更改
                Log.e(TAG, "获取资源包信息失败", e)
                state.emit(State.Error("获取资源包信息失败"))
                return
            }
        }

        resPacks.emit(packs)
        errors.emit(mappedErrors)
        state.emit(State.Done)
    }

    fun selectedFileToInstall(path: String) {
        viewModelScope.launch {
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
                val task = resManager.install(respack)
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
                refresh()
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