package org.wvt.horizonmgr.ui.locale

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class LocaleManagerViewModel : ViewModel() {
    val tabs = Tabs.values().toList()
    var selectedTab = MutableStateFlow(Tabs.MOD)
}