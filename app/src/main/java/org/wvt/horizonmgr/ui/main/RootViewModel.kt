package org.wvt.horizonmgr.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import org.wvt.horizonmgr.DependenciesContainer

class RootViewModel(private val dependencies: DependenciesContainer) : ViewModel() {

    enum class Screen(
        val label: String,
        val icon: ImageVector
    ) {
        HOME("首页", Icons.Filled.Home),
        PACKAGE_MANAGE("分包管理", Icons.Filled.Dashboard),
        LOCAL_MANAGE("模组管理", Icons.Filled.Extension),
        ONLINE_DOWNLOAD("在线资源", Icons.Filled.Store),
        DOWNLOADED_MOD("本地资源", Icons.Filled.Storage)
    }

    var currentScreen by mutableStateOf(Screen.LOCAL_MANAGE)
        private set

    fun navigate(screen: Screen) {
        currentScreen = screen
    }
}