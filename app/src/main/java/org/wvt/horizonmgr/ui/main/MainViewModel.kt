package org.wvt.horizonmgr.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.utils.LocalCache
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val localCache: LocalCache
) : ViewModel() {
    val userInfo = MutableStateFlow<UserInformation?>(null)

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

    fun logOut() {
        viewModelScope.launch(Dispatchers.IO) {
            localCache.clearCachedUserInfo()
            userInfo.value = null
        }
    }

    fun resume() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                userInfo.value = localCache.getCachedUserInfo()?.let {
                    UserInformation(it.name, it.account, it.avatarUrl)
                }
            }
        } catch (e: Exception) {
            // TODO: 2021/5/21
        }
    }
}