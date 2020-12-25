package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.ui.components.ProgressDialogState

class ModuleManagerViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {
    enum class Tabs(val label: String) {
        MOD("Mod"), IC_MAP("IC地图"), MC_MAP("MC地图"), IC_TEXTURE("IC材质"), MC_TEXTURE("MC材质")
    }
    
    private var selectedPackageUUID: String? = null

    private val _selectedPackage = MutableStateFlow(true)
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

    fun selectTab(tab: Tabs) {
        _selectedTab.value = tab
    }
}