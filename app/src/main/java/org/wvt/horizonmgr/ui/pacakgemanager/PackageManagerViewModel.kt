package org.wvt.horizonmgr.ui.pacakgemanager

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.ui.components.InputDialogHostState
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import org.wvt.horizonmgr.ui.startActivity
import java.text.SimpleDateFormat
import java.util.*

class PackageManagerViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {
    private val horizonMgr = dependencies.horizonManager

    private val _packages = MutableStateFlow(emptyList<PackageManagerItem>())
    val packages: StateFlow<List<PackageManagerItem>> = _packages
    private var selectedPackage: String? = null

    private val _progressState = MutableStateFlow<ProgressDialogState?>(null)
    val progressState: StateFlow<ProgressDialogState?> = _progressState

    init {
        loadPackages()
    }

    fun loadPackages() {
        viewModelScope.launch {
            try {
                val dateFormatter = SimpleDateFormat.getDateInstance()
                _packages.value = horizonMgr.getLocalPackages().map {
                    PackageManagerItem(
                        it.uuid,
                        it.customName,
                        dateFormatter.format(Date(it.installTimeStamp)),
                        "无额外描述"
                    )
                }
            } catch (e: Exception) {
                // TODO Display error message
            }
        }
    }

    fun setSelectedPackage(uuid: String?) {
        selectedPackage = uuid

    }

    fun deletePackage(
        uuid: String,
        confirmDeleteDialogHostState: ConfirmDeleteDialogHostState
    ) {
        viewModelScope.launch {
            if (confirmDeleteDialogHostState.showDialog() ==
                ConfirmDeleteDialogHostState.DialogResult.CONFIRM
            ) {
                _progressState.value = ProgressDialogState.Loading("正在删除")
                try {
                    horizonMgr.deletePackage(uuid)
                } catch (e: Exception) {
                    _progressState.value =
                        ProgressDialogState.Failed("删除失败", e.localizedMessage ?: "")
                    return@launch
                }
                _progressState.value = ProgressDialogState.Finished("删除成功")
                loadPackages()
            }
        }
    }

    fun renamePackage(uuid: String, inputDialogHostState: InputDialogHostState) {
        viewModelScope.launch {
            val result =
                inputDialogHostState.showDialog("重命名", "请输入新名称")
            if (result is InputDialogHostState.DialogResult.Confirm) {
                _progressState.value = ProgressDialogState.Loading("正在重命名")
                try {
                    horizonMgr.renamePackage(uuid, result.name)
                } catch (e: Exception) {
                    _progressState.value =
                        ProgressDialogState.Failed("重命名失败", e.localizedMessage ?: "")
                    return@launch
                }
                _progressState.value = ProgressDialogState.Finished("重命名完成")
                loadPackages()
            }
        }
    }

    fun clonePackage(uuid: String, inputDialogHostState: InputDialogHostState) {
        viewModelScope.launch {
            val result =
                inputDialogHostState.showDialog("克隆", "请输入新名称")
            if (result is InputDialogHostState.DialogResult.Confirm) {
                _progressState.value = ProgressDialogState.Loading("正在克隆")
                try {
                    horizonMgr.clonePackage(uuid, result.name)
                } catch (e: Exception) {
                    _progressState.value =
                        ProgressDialogState.Failed("克隆失败", e.localizedMessage ?: "")
                    return@launch
                }
                _progressState.value = ProgressDialogState.Finished("克隆完成")
                loadPackages()
            }
        }
    }

    fun dismiss() {
        _progressState.value = null
    }

    fun startInstallPackageActivity(context: Context) {
        context.startActivity<InstallPackageActivity>()
    }

    fun showInfo(context: Context, modId: String) {
        PackageDetailActivity.start(context, modId)
    }
}