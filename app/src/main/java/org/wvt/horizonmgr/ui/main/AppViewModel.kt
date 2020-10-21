package org.wvt.horizonmgr.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Nature
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel() : ViewModel() {

    enum class Screen(
        val label: String,
        val icon: VectorAsset
    ) {
        PACKAGE_MANAGE("分包管理", Icons.Filled.Extension),
        LOCAL_MANAGE("模组管理", Icons.Filled.Nature),
        ONLINE_DOWNLOAD("在线资源", Icons.Filled.GetApp),
        DOWNLOADED_MOD("本地资源", Icons.Filled.Cached)
    }

    private val _currentScreen = MutableStateFlow(Screen.LOCAL_MANAGE)
    val currentScreen: StateFlow<Screen> = _currentScreen

    fun navigate(screen: Screen) {
        _currentScreen.value = screen
    }

    fun navigateToPackageManage() {}
    fun navigateToLocalManage() {}
    fun navigateToOnlineDownload() {}
    fun navigateToDownloadedMod() {}
}