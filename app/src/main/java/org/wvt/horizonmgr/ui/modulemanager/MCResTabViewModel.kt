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
import org.wvt.horizonmgr.service.respack.ResourcePackManifest
import org.wvt.horizonmgr.service.respack.ZipResourcePackage
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import java.io.File

private const val TAG = "MCResTabVM"

class MCResTabViewModel(dependencies: DependenciesContainer) : ViewModel() {
    private val resManager = dependencies.mcResourcePackManager

    val resPacks = MutableStateFlow<List<ResPack>>(emptyList())

    val state = MutableStateFlow<State>(State.LOADING)

    val progressState = MutableStateFlow<ProgressDialogState?>(null)

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