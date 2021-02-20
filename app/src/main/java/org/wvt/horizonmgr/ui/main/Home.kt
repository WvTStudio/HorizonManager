package org.wvt.horizonmgr.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.components.MyAlertDialog
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.downloaded.DownloadedMods
import org.wvt.horizonmgr.ui.modulemanager.ModuleManager
import org.wvt.horizonmgr.ui.news.News
import org.wvt.horizonmgr.ui.onlinemods.Online
import org.wvt.horizonmgr.ui.pacakgemanager.PackageManager
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@Deprecated("Deprecated")
val LocalSelectedPackageUUID = staticCompositionLocalOf<String?>()
val LocalDrawerState = staticCompositionLocalOf<DrawerState>()

@Composable
fun Home(
    viewModel: HomeViewModel,
    userInfo: UserInformation?,
    requestLogin: () -> Unit,
    requestLogout: () -> Unit,
    selectedPackageUUID: String?,
    selectedPackageChange: (uuid: String?) -> Unit,
    requestOnlineInstall: () -> Unit,
    onAddModClicked: () -> Unit,
    onAddPackageClicked: () -> Unit,
    community: () -> Unit,
    openGame: () -> Unit,
    joinGroup: () -> Unit,
    donate: () -> Unit,
    settings: () -> Unit,
    navigateToPackageInfo: (uuid: String) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val screens by remember { mutableStateOf(HomeViewModel.Screen.values()) }

    Drawer(
        state = drawerState,
        header = {
            DrawerHeader(
                userInfo = userInfo,
                requestLogin = requestLogin,
                requestLogout = requestLogout
            )
        },
        tabs = {
            DrawerTabs(screens = screens, currentScreen = viewModel.currentScreen, onChange = {
                viewModel.navigate(it)
                drawerState.close()
            })
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
            LocalDrawerState provides drawerState,
            LocalSelectedPackageUUID provides selectedPackageUUID
        ) {
            Crossfade(targetState = viewModel.currentScreen) { cs ->
                when (cs) {
                    HomeViewModel.Screen.HOME -> News(
                        onNavClick = { drawerState.open() }
                    )
                    HomeViewModel.Screen.LOCAL_MANAGE -> ModuleManager(
                        onNavClicked = { drawerState.open() },
                        onAddModClicked = onAddModClicked,
                    )
                    HomeViewModel.Screen.PACKAGE_MANAGE -> PackageManager(
                        onPackageSelect = selectedPackageChange,
                        onNavClick = { drawerState.open() },
                        onOnlineInstallClick = requestOnlineInstall,
                        onLocalInstallClick = onAddPackageClicked,
                        navigateToPackageInfo = navigateToPackageInfo
                    )
                    HomeViewModel.Screen.ONLINE_DOWNLOAD -> Online(
                        enable = userInfo != null,
                        onNavClicked = { drawerState.open() }
                    )
                    HomeViewModel.Screen.DOWNLOADED_MOD -> DownloadedMods(
                        onNavClicked = { drawerState.open() }
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerTabs(
    screens: Array<HomeViewModel.Screen>,
    currentScreen: HomeViewModel.Screen,
    onChange: (HomeViewModel.Screen) -> Unit,
) {
    screens.forEach {
        NavigationItem(
            checked = currentScreen == it,
            onCheckedChange = { checked -> if (checked) onChange(it) },
            text = it.label, icon = it.icon
        )
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
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        header()
                        Divider(Modifier.padding(top = 16.dp))
                        Column(Modifier.padding(vertical = 8.dp)) { tabs() }
                        Divider()
                        Column(Modifier.padding(vertical = 8.dp)) { items() }
                    }
                }
                Divider()
                // Settings
                Column(Modifier.padding(vertical = 8.dp)) { setting() }
            }
        },
        bodyContent = content
    )
}

data class UserInformation(
    val name: String,
    val account: String,
    val avatarUrl: String
)

@Composable
private fun DrawerHeader(
    userInfo: UserInformation?,
    requestLogin: () -> Unit,
    requestLogout: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        MyAlertDialog(
            modifier = Modifier.shadow(16.dp, clip = false),
            onDismissRequest = { showDialog = false },
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
            }
        )
    }
    // Avatar
    Column(Modifier.padding(16.dp)) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
        ) {
            Box(Modifier.fillMaxSize().clickable {
                if (userInfo == null) requestLogin()
                else showDialog = true
            }) {
                userInfo?.let {
                    NetworkImage(
                        url = it.avatarUrl,
                        contentDescription = "头像",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
    icon: ImageVector
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = animateColorAsState(if (checked) MaterialTheme.colors.primary.copy(alpha = 0.12f) else Color.Transparent).value,
        contentColor = animateColorAsState(if (checked) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface).value,
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Providers(LocalContentAlpha provides ContentAlpha.high) {
            Row(
                Modifier.fillMaxSize()
                    .clickable { onCheckedChange(!checked) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    modifier = Modifier.padding(start = 8.dp),
                    contentDescription = null
                )
                Text(
                    text = text,
                    modifier = Modifier.padding(start = 24.dp, end = 8.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
@Preview
private fun DrawerPreview() {
    PreviewTheme {
        val state = rememberDrawerState(DrawerValue.Open)
        Drawer(state = state, header = {
            DrawerHeader(userInfo = null, requestLogin = {}, requestLogout = {})
        }, tabs = {
            DrawerTabs(
                screens = HomeViewModel.Screen.values(),
                currentScreen = HomeViewModel.Screen.HOME,
                onChange = {}
            )
        }, items = {}, setting = {}) {}
    }
}
