package org.wvt.horizonmgr.ui.pacakgemanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.hzpack.*
import org.wvt.horizonmgr.ui.components.InputDialogHostState
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "PackageManagerVM"

class PackageManagerViewModel(
    dependencies: DependenciesContainer
) : ViewModel() {
    private val mgr = dependencies.manager
    private val _packages = MutableStateFlow(emptyList<PackageManagerItem>())
    val packages: StateFlow<List<PackageManagerItem>> = _packages.asStateFlow()

    private val _progressState = MutableStateFlow<ProgressDialogState?>(null)
    val progressState: StateFlow<ProgressDialogState?> = _progressState.asStateFlow()

    private var cachedPackages: List<InstalledPackage> = emptyList()
    private var selectedPackage: InstalledPackage? = null
    private var selectedPackageUUID: String? = null

    var state = MutableStateFlow<State>(State.Loading)

    sealed class State {
        object Loading : State()
        class Error(val message: String, val detail: String?) : State()
        object OK : State()
    }

    val errors = MutableStateFlow<List<Exception>>(emptyList())

    fun loadPackages() {
        viewModelScope.launch {
            state.emit(State.Loading)
            val dateFormatter = SimpleDateFormat.getDateInstance()
            val packs = try {
                mgr.getInstalledPackages()
            } catch (e: Exception) {
                Log.e(TAG, "获取分包失败", e)
                state.emit(State.Error("获取分包失败", e.message))
                return@launch
            }
            Log.d(TAG, "获取到 ${packs.size} 个分包")
            val exceptions = mutableListOf<Exception>()
            val result = packs.mapNotNull {
                val installInfo: InstallationInfo
                val manifest: PackageManifest
                try {
                    installInfo = it.getInstallationInfo()
                    manifest = it.getManifest()
                } catch (e: Exception) {
                    exceptions.add(e)
                    return@mapNotNull null
                }
                PackageManagerItem(
                    uuid = installInfo.internalId,
                    name = installInfo.customName ?: manifest.pack,
                    timeStr = dateFormatter.format(Date(installInfo.timeStamp)),
                    description = manifest.recommendDescription()
                )
            }
            cachedPackages = packs
            _packages.emit(result)
            errors.emit(exceptions)
            state.emit(State.OK)
        }
    }

    fun setSelectedPackage(uuid: String?) {
        viewModelScope.launch {
            if (uuid == null) {
                selectedPackageUUID = null
                selectedPackage = null
            } else {
                try {
                    mgr.getInstalledPackages().find { it.getInstallationInfo().internalId == uuid }
                } catch (e: Exception) {
                    state.emit(State.Error("获取分包失败", e.message))
                    return@launch
                }
                selectedPackageUUID = uuid
            }
        }
    }

    fun deletePackage(
        uuid: String,
        confirmDeleteDialogHostState: ConfirmDeleteDialogHostState,
        onSucceed: () -> Unit
    ) {
        viewModelScope.launch {
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
                onSucceed()
                loadPackages()
            }
        }
    }

    fun renamePackage(uuid: String, inputDialogHostState: InputDialogHostState) {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
            try {
                mgr.installPackage(ZipPackage(File(filePath)), null)
            } catch (e: Exception) {
                _progressState.emit(ProgressDialogState.Failed("安装失败", "安装失败，请检查文件格式是否正确"))
            }
            _progressState.emit(ProgressDialogState.Finished("安装完成"))
            loadPackages()
        }
    }
}