package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import java.io.File

class LocaleManagerViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {
    enum class Tabs(val label: String) {
        MOD("Mod"), IC_MAP("IC地图"), MC_MAP("MC地图"), IC_TEXTURE("IC材质"), MC_TEXTURE("MC材质")
    }

    private val horizonMgr = dependencies.horizonManager

    private var selectedPackageUUID: String? = null

    private val _selectedPackage = MutableStateFlow(true)
    val selectedPackage: StateFlow<Boolean> = _selectedPackage

    init {
        viewModelScope.launch {
            selectedPackageUUID = dependencies.localCache.getSelectedPackageUUID()
            _selectedPackage.value = selectedPackageUUID != null
        }
    }

    @Stable
    val tabs = Tabs.values().toList()

    private val _selectedTab = MutableStateFlow(Tabs.MOD)
    val selectedTab: StateFlow<Tabs> = _selectedTab

    private val _progressState = MutableStateFlow<ProgressDialogState?>(null)
    val progressState: StateFlow<ProgressDialogState?> = _progressState


    fun install(requestSelectFile: suspend () -> String?) {
        viewModelScope.launch {
            val path = requestSelectFile() ?: return@launch
            try {
                _progressState.value = ProgressDialogState.Loading("正在安装")
                if (horizonMgr.getFileType(File(path)) != HorizonManager.FileType.Mod)
                    error("不是Mod")
                selectedPackageUUID?.let {
                    horizonMgr.installMod(it, File(path))
                }
            } catch (e: Exception) {
                _progressState.value =
                    ProgressDialogState.Failed("安装失败", "请检查您选择的文件格式是否正确")
                return@launch
            }
            _progressState.value = ProgressDialogState.Finished("安装完成")
        }
    }

    fun dismiss() {
        _progressState.value = null
    }

    fun selectTab(tab: Tabs) {
        _selectedTab.value = tab
    }
}