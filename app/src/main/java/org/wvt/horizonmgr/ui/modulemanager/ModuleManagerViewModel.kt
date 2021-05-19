package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.wvt.horizonmgr.DependenciesContainer
import javax.inject.Inject

@HiltViewModel
class ModuleManagerViewModel @Inject constructor(dependencies: DependenciesContainer) : ViewModel() {
    enum class Tabs(val label: String) {
        MOD("Mod"), IC_MAP("IC地图"), MC_MAP("MC地图"), IC_TEXTURE("IC材质"), MC_TEXTURE("MC材质")
    }

    @Stable
    val tabs = Tabs.values().toList()

    private val _selectedTab = MutableStateFlow(Tabs.MOD)
    val selectedTab: StateFlow<Tabs> = _selectedTab

    fun selectTab(tab: Tabs) {
        _selectedTab.value = tab
    }
}