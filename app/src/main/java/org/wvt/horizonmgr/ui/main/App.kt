package org.wvt.horizonmgr.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animate
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.ui.`package`.PackageManager
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.downloaded.DownloadedMods
import org.wvt.horizonmgr.ui.locale.LocalManager
import org.wvt.horizonmgr.ui.onlinemod.Online

val UserInfoAmbient = staticAmbientOf<WebAPI.UserInfo?>()
val SelectedPackageUUIDAmbient = staticAmbientOf<String?>()
val DrawerStateAmbient = staticAmbientOf<DrawerState>()

private enum class Screen(
    val label: String,
    val icon: VectorAsset
) {
    PACKAGE_MANAGE("分包管理", Icons.Filled.Extension),
    LOCAL_MANAGE("模组管理", Icons.Filled.Nature),
    ONLINE_DOWNLOAD("在线资源", Icons.Filled.GetApp),
    DOWNLOADED_MOD("本地资源", Icons.Filled.Cached)
}

@Composable
fun App(
    userInfo: WebAPI.UserInfo?,
    requestLogin: () -> Unit,
    requestLogout: () -> Unit,
    selectedPackageUUID: String?,
    selectedPackageChange: (uuid: String?) -> Unit,
    community: () -> Unit,
    openGame: () -> Unit,
    joinGroup: () -> Unit,
    donate: () -> Unit,
    settings: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val screens by remember { mutableStateOf(Screen.values()) }
    var currentScreen by savedInstanceState { Screen.LOCAL_MANAGE }

    Drawer(
        state = drawerState, header =  {DrawerHeader(
            userInfo = userInfo,
            requestLogin = requestLogin,
            requestLogout = requestLogout
        )},
        tabs = {
            screens.forEach {
                NavigationItem(
                    checked = currentScreen == it, onCheckedChange = { checked ->
                        if (checked) currentScreen = it
                        drawerState.close()
                    }, text = it.label, icon = it.icon
                )
            }
        },
        items = {
            NavigationItem(
                checked = false, onCheckedChange = { community() },
                text = "中文社区", icon = Icons.Filled.Forum
            )
            NavigationItem(
                checked = false, onCheckedChange = { openGame() },
                text = "进入游戏", icon = Icons.Filled.Gamepad
            )
            NavigationItem(
                checked = false, onCheckedChange = { joinGroup() },
                text = "加入群组", icon = Icons.Filled.Group
            )
            NavigationItem(
                checked = false, onCheckedChange = { donate() },
                text = "捐赠作者", icon = Icons.Filled.AttachMoney
            )
        },
        setting = {
            NavigationItem(
                checked = false, onCheckedChange = { settings() },
                text = "设置", icon = Icons.Filled.Settings
            )
        }
    ) {
        Providers(
            DrawerStateAmbient provides drawerState,
            UserInfoAmbient provides userInfo,
            SelectedPackageUUIDAmbient provides selectedPackageUUID
        ) {
            Crossfade(current = currentScreen, modifier = Modifier.fillMaxSize()) {
                when (it) {
                    Screen.LOCAL_MANAGE -> LocalManager(selectedPackageUUID = selectedPackageUUID)
                    Screen.PACKAGE_MANAGE -> PackageManager(
                        selectedPackageUUID = selectedPackageUUID,
                        onPackageSelect = selectedPackageChange
                    )
                    Screen.ONLINE_DOWNLOAD -> Online(onDownloadClick = {})
                    Screen.DOWNLOADED_MOD -> DownloadedMods()
                }
            }
        }
    }
}

@Composable
private fun Drawer(
    state: DrawerState,
    header: @Composable () -> Unit,
    tabs: @Composable () -> Unit,
    items: @Composable () -> Unit,
    setting: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    ModalDrawerLayout(
        drawerState = state,
        drawerContent = {
            Column(modifier = Modifier.fillMaxSize()) {
                ScrollableColumn(modifier = Modifier.weight(1f)) {
                    header()
                    Divider(Modifier.padding(vertical = 16.dp))
                    Column { tabs() }
                    Divider(Modifier.padding(vertical = 16.dp))
                    Column { items() }
                }
                Divider(Modifier.padding(top = 16.dp))
                // Settings
                Column(Modifier.padding(vertical = 8.dp)) { setting() }
            }
        },
        bodyContent = content
    )
}

@Composable
private fun DrawerHeader(
    userInfo: WebAPI.UserInfo?,
    requestLogin: () -> Unit,
    requestLogout: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AlertDialog(onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    requestLogout()
                }) {
                    Text("注销")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("取消") }
            }, title = {
                Text("是否注销登录？")
            }, text = {
                Text("注销后需要重新登录才能使用在线下载功能")
            })
    }
    // Avatar
    Column(Modifier.padding(16.dp)) {
        val interactionState = remember { InteractionState() }
        Surface(
            modifier = Modifier.size(48.dp)
                .clickable(
                    onClick = {
                        if (userInfo == null) requestLogin()
                        else showDialog = true
                    },
                    interactionState = interactionState, indication = null
                ),
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
        ) {
            userInfo?.let {
                NetworkImage(
                    url = it.avatarUrl,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.indication(
                        interactionState,
                        IndicationAmbient.current()
                    )
                        .fillMaxSize()
                )
            }
        }
        Text(
            if (userInfo == null) "欢迎！点击头像登录" else userInfo.name + "，欢迎！",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.h5
        )
        Text(
            userInfo?.account ?: "WvT工作室制作",
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@Composable
private fun NavigationItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    icon: VectorAsset
) {
    val interactionState = remember { InteractionState() }
    val emphasis = EmphasisAmbient.current
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = animate(if (checked) MaterialTheme.colors.primary.copy(alpha = 0.12f) else Color.Transparent),
        contentColor = animate(if (checked) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface),
        modifier = Modifier.clickable(
            onClick = { onCheckedChange(!checked) },
            interactionState = interactionState,
            indication = null
        ).height(48.dp).fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        ProvideEmphasis(emphasis = emphasis.high) {
            Row(
                Modifier.fillMaxSize()
                    .indication(interactionState, IndicationAmbient.current()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(asset = icon, modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = text,
                    modifier = Modifier.padding(start = 24.dp, end = 8.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

