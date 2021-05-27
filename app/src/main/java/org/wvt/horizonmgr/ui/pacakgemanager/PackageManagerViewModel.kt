package org.wvt.horizonmgr.ui.pacakgemanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.service.hzpack.*
import org.wvt.horizonmgr.ui.components.InputDialogHostState
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import org.wvt.horizonmgr.utils.LocalCache
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val TAG = "PackageManagerVM"

@HiltViewModel
class PackageManagerViewModel @Inject constructor(
    private val mgr: HorizonManager,
    private val localCache: LocalCache
) : ViewModel() {
    private val _packages = MutableStateFlow(emptyList<PackageManagerItem>())
    private val _progressState = MutableStateFlow<ProgressDialogState?>(null)
    private val dateFormatter = SimpleDateFormat.getDateInstance()
    private var cachedPackages: List<InstalledPackage> = emptyList()

    val state = MutableStateFlow<State>(State.Initializing)
    val errors = MutableStateFlow<List<String>>(emptyList())
    val isRefreshing = MutableStateFlow(false)
    val progressState: StateFlow<ProgressDialogState?> = _progressState.asStateFlow()
    val packages: StateFlow<List<PackageManagerItem>> = _packages.asStateFlow()
    val selectedPackage = MutableStateFlow<String?>(null)

    sealed class State {
        object Initializing : State()
        class Error(val message: String, val detail: String?) : State()
        object OK : State()
    }

    fun loadPackages() {
        viewModelScope.launch(Dispatchers.IO) {
            if (state.value == State.Initializing) {
                loadData()
                loadSelectedPackage()
            } else {
                isRefreshing.emit(true)
                loadData()
                loadSelectedPackage()
                isRefreshing.emit(false)
            }
        }
    }

    private suspend fun loadData() {
        val getResult = try {
            mgr.getInstalledPackages()
        } catch (e: Exception) {
            Log.e(TAG, "获取分包失败", e)
            state.emit(State.Error("获取分包失败", e.message))
            return
        }
        Log.d(TAG, "获取到 ${getResult.packages.size} 个分包")
        val mappedExceptions = getResult.errors.map {
            "${it.file}: ${it.error.message ?: "未知错误"}"
        }.toMutableList()
        val result = getResult.packages.mapNotNull {
            val installInfo: InstallationInfo
            val manifest: PackageManifest

            try {
                installInfo = it.getInstallationInfo()
                manifest = it.getManifest()
            } catch (e: Exception) {
                mappedExceptions.add("${it.packageDirectory}: ${e.message ?: "未知错误"}")
                Log.d(TAG, "分包解析失败", e)
                return@mapNotNull null
            }
            PackageManagerItem(
                uuid = installInfo.internalId,
                name = installInfo.customName ?: manifest.pack,
                timeStr = dateFormatter.format(Date(installInfo.timeStamp)),
                description = manifest.recommendDescription()
            )
        }
        cachedPackages = getResult.packages
        delay(200)
        _packages.emit(result)
        errors.emit(mappedExceptions)


        state.emit(State.OK)
    }

    private suspend fun loadSelectedPackage() {
        val uuid = localCache.getSelectedPackageUUID()
        val selectedPackage = uuid?.let { uuid ->
            cachedPackages.find {
                it.getInstallationInfo().internalId == uuid
            }
        }
        if (selectedPackage != null) {
            this.selectedPackage.emit(uuid)
        } else {
            this.selectedPackage.emit(null)
        }
    }

    fun selectPackage(uuid: String?) {
        viewModelScope.launch {
            localCache.setSelectedPackageUUID(uuid)
            selectedPackage.emit(uuid)
        }
    }

    fun deletePackage(
        uuid: String,
        confirmDeleteDialogHostState: ConfirmDeleteDialogHostState
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (confirmDeleteDialogHostState.showDialog() ==
                ConfirmDeleteDialogHostState.DialogResult.CONFIRM
            ) {
                _progressState.emit(ProgressDialogState.Loading("正在删除"))
                try {
                    cachedPackages.find { it.getInstallationInfo().internalId == uuid }?.delete()
                } catch (e: Exception) {
                    Log.e(TAG, "删除分包失败", e)
                    _progressState.emit(
                        ProgressDialogState.Failed("删除失败", e.localizedMessage ?: "")
                    )
                    return@launch
                }
                _progressState.emit(ProgressDialogState.Finished("删除成功"))
                localCache.setSelectedPackageUUID(null)
                selectedPackage.emit(null)
                loadPackages()
            }
        }
    }

    fun renamePackage(uuid: String, inputDialogHostState: InputDialogHostState) {
        viewModelScope.launch(Dispatchers.IO) {
            val pkg =
                cachedPackages.find { it.getInstallationInfo().internalId == uuid } ?: return@launch
            val (manifest, installationInfo) = try {
                pkg.getManifest() to pkg.getInstallationInfo()
            } catch (e: Exception) {
                _progressState.emit(
                    ProgressDialogState.Failed("获取分包信息失败", e.localizedMessage ?: "")
                )
                return@launch
            }

            val result =
                inputDialogHostState.showDialog(
                    installationInfo.customName ?: manifest.pack,
                    "重命名",
                    "请输入新名称"
                )
            if (result is InputDialogHostState.DialogResult.Confirm) {
                _progressState.emit(ProgressDialogState.Loading("正在重命名"))
                try {
                    pkg.rename(result.input)
                } catch (e: Exception) {
                    Log.e(TAG, "重命名分包失败", e)
                    _progressState.emit(
                        ProgressDialogState.Failed("重命名失败", e.localizedMessage ?: "")
                    )
                    return@launch
                }
                _progressState.emit(ProgressDialogState.Finished("重命名完成"))
                loadPackages()
            }
        }
    }

    fun clonePackage(uuid: String, inputDialogHostState: InputDialogHostState) {
        viewModelScope.launch(Dispatchers.IO) {
            val pkg =
                cachedPackages.find { it.getInstallationInfo().internalId == uuid } ?: return@launch
            val (manifest, installationInfo) = try {
                pkg.getManifest() to pkg.getInstallationInfo()
            } catch (e: Exception) {
                _progressState.emit(
                    ProgressDialogState.Failed("获取分包信息失败", e.localizedMessage ?: "")
                )
                return@launch
            }
            val result = inputDialogHostState.showDialog(
                installationInfo.customName ?: manifest.pack,
                "克隆",
                "请输入新名称"
            )
            if (result is InputDialogHostState.DialogResult.Confirm) {
                _progressState.emit(ProgressDialogState.Loading("正在克隆"))
                try {
                    pkg.clone(result.input)
                } catch (e: Exception) {
                    Log.e(TAG, "克隆分包失败", e)
                    _progressState.emit(
                        ProgressDialogState.Failed("克隆失败", e.localizedMessage ?: "")
                    )
                    return@launch
                }
                _progressState.emit(ProgressDialogState.Finished("克隆完成"))
                loadPackages()
            }
        }
    }

    fun dismiss() {
        viewModelScope.launch {
            _progressState.emit(null)
        }
    }

    fun selectedFile(filePath: String) {
        // TODO: 2021/2/20 实现选择文件安装
        viewModelScope.launch {
            _progressState.emit(ProgressDialogState.Loading("正在安装"))
            val zipPackage = withContext(Dispatchers.IO) {
                ZipPackage(File(filePath))
            }
            if (!zipPackage.isZipPackage()) {
                _progressState.emit(ProgressDialogState.Failed("解析失败", "您选择的文件可能不是一个正确的分包"))
                return@launch
            }
            try {
                // TODO: 2021/5/27 允许自定义一些配置
                withContext(Dispatchers.IO) {
                    mgr.installPackage(
                        ZipPackage(File(filePath)),
                        null,
                        null,
                        null
                    )
                }
            } catch (e: Exception) {
                _progressState.emit(ProgressDialogState.Failed("安装失败", "安装过程中出现错误"))
                return@launch
            }
            _progressState.emit(ProgressDialogState.Finished("安装完成"))
            loadPackages()
        }
    }
}