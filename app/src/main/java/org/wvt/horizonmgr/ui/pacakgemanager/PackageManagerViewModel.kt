package org.wvt.horizonmgr.ui.pacakgemanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.hzpack.InstalledPackage
import org.wvt.horizonmgr.service.hzpack.ZipPackage
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

    fun loadPackages() {
        viewModelScope.launch {
            val dateFormatter = SimpleDateFormat.getDateInstance()
            val result = try {
                cachedPackages = mgr.getInstalledPackages()
                Log.d(TAG, "获取到 ${cachedPackages.size} 个分包")
                cachedPackages.map {
                    PackageManagerItem(
                        uuid = it.getInstallUUID(),
                        name = it.getCustomName() ?: it.getName(),
                        timeStr = dateFormatter.format(Date(it.getInstallTimeStamp())),
                        description = it.getDescription()["en"] ?: "无描述"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取分包失败", e)
                // TODO: 2021/2/20 Displays error message
                return@launch
            }
            _packages.emit(result)
        }
    }

    fun setSelectedPackage(uuid: String?) {
        viewModelScope.launch {
            if (uuid == null) {
                selectedPackageUUID = null
                selectedPackage = null
            } else {
                selectedPackage = mgr.getInstalledPackages().find { it.getInstallUUID() == uuid }
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
                    cachedPackages.find {
                        it.getInstallUUID() == uuid
                    }?.delete()
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
            val pkg = cachedPackages.find {
                it.getInstallUUID() == uuid
            } ?: return@launch

            val result =
                inputDialogHostState.showDialog(pkg.getCustomName() ?: pkg.getName(), "重命名", "请输入新名称")
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
            val pkg = cachedPackages.find {
                it.getInstallUUID() == uuid
            } ?: return@launch

            val result = inputDialogHostState.showDialog(pkg.getCustomName() ?: pkg.getName(), "克隆", "请输入新名称")
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
                mgr.installPackage(ZipPackage.parse(File(filePath)), null)
            } catch (e: Exception) {
                _progressState.emit(ProgressDialogState.Failed("安装失败", "安装失败，请检查文件格式是否正确"))
            }
            _progressState.emit(ProgressDialogState.Finished("安装完成"))
            loadPackages()
        }
    }
}