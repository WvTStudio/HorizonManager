package org.wvt.horizonmgr.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.downloaded.DownloadedMods
import org.wvt.horizonmgr.ui.home.HomeScreen
import org.wvt.horizonmgr.ui.home.HomeViewModel
import org.wvt.horizonmgr.ui.modulemanager.ModuleManagerScreen
import org.wvt.horizonmgr.ui.onlinemods.OnlineMods
import org.wvt.horizonmgr.ui.pacakgemanager.PackageManagerScreen
import org.wvt.horizonmgr.ui.theme.LocalThemeConfig
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    requestOpenGame: () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToOnlineInstall: () -> Unit,
    navigateToCommunity: () -> Unit,
    navigateToJoinGroup: () -> Unit,
    navigateToDonate: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToPackageDetail: (uuid: String) -> Unit,
    navigateToArticleDetail: (articleId: String) -> Unit,
    onAddModClicked: () -> Unit,
    onAddPackageClicked: () -> Unit,
    onAddICLevelClick: () -> Unit,
    onAddMCLevelClick: () -> Unit,
    onAddICTextureClick: () -> Unit,
    onAddMCTextureClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val screens by remember { mutableStateOf(MainViewModel.Screen.values()) }
    val userInfo by viewModel.userInfo.collectAsState()

    LaunchedEffect(Unit) { viewModel.resume() }

    Drawer(
        state = drawerState,
        header = {
            DrawerHeader(
                userInfo = userInfo,
                requestLogin = navigateToLogin,
                requestLogout = { viewModel.logOut() }
            )
        },
        tabs = {
            DrawerTabs(screens = screens, currentScreen = viewModel.currentScreen, onChange = {
                viewModel.navigate(it)
                scope.launch { drawerState.close() }
            })
        },
        items = {
            NavigationItem(
                checked = false, onCheckedChange = { navigateToCommunity() },
                text = "中文社区", icon = Icons.Filled.Forum
            )
            NavigationItem(
                checked = false, onCheckedChange = { requestOpenGame() },
                text = "进入游戏", icon = Icons.Filled.Gamepad
            )
            NavigationItem(
                checked = false, onCheckedChange = { navigateToJoinGroup() },
                text = "加入群组", icon = Icons.Filled.Group
            )
            NavigationItem(
                checked = false, onCheckedChange = { navigateToDonate() },
                text = "捐赠作者", icon = Icons.Filled.AttachMoney
            )
        },
        setting = {
            NavigationItem(
                checked = false, onCheckedChange = { navigateToSettings() },
                text = "设置", icon = Icons.Filled.Settings
            )
        }
    ) {
        Crossfade(targetState = viewModel.currentScreen) { cs ->
            when (cs) {
                MainViewModel.Screen.HOME -> HomeScreen(
                    onNavClick = { scope.launch { drawerState.open() } },
                    onNewsClick = {
                        if (it is HomeViewModel.ContentResource.Article) {
                            navigateToArticleDetail(it.id)
                        }
                    }
                )
                MainViewModel.Screen.LOCAL_MANAGE -> ModuleManagerScreen(
                    onNavClicked = { scope.launch { drawerState.open() } },
                    onAddModClicked = onAddModClicked,
                    onAddICLevelClick = onAddICLevelClick,
                    onAddICTextureClick = onAddICTextureClick,
                    onAddMCLevelClick = onAddMCLevelClick,
                    onAddMCTextureClick = onAddMCTextureClick
                )
                MainViewModel.Screen.PACKAGE_MANAGE -> PackageManagerScreen(
                    onNavClick = { scope.launch { drawerState.open() } },
                    onOnlineInstallClick = navigateToOnlineInstall,
                    onLocalInstallClick = onAddPackageClicked,
                    navigateToPackageInfo = navigateToPackageDetail
                )
                MainViewModel.Screen.ONLINE_DOWNLOAD -> OnlineMods(
                    viewModel = hiltViewModel(),
                    isLogon = userInfo != null,
                    onNavClick = { scope.launch { drawerState.open() } }
                )
                MainViewModel.Screen.DOWNLOADED_MOD -> DownloadedMods(
                    vm = hiltViewModel(),
                    onNavClicked = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}

@Composable
private fun DrawerTabs(
    screens: Array<MainViewModel.Screen>,
    currentScreen: MainViewModel.Screen,
    onChange: (MainViewModel.Screen) -> Unit,
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
    ModalDrawer(
        drawerState = state,
        drawerContent = {
            val theme = LocalThemeConfig.current
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    header()
                    if (theme.isDark || !theme.appbarAccent) {
                        Divider(Modifier.fillMaxWidth())
                    }
                    Column(Modifier.padding(vertical = 8.dp)) { tabs() }
                    Divider(Modifier.fillMaxWidth())
                    Column(Modifier.padding(vertical = 8.dp)) { items() }
                }
                Divider()
                // Settings
                Column(Modifier.padding(vertical = 8.dp)) { setting() }
            }
        },
        content = content
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
    val theme = LocalThemeConfig.current
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AlertDialog(
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = theme.appbarColor
    ) {
        val contentColor = LocalContentColor.current
        val gear = painterResource(id = R.drawable.ic_gear_full)

        val gearRotation = remember { Animatable(0f) }

        Box {
            BoxWithConstraints(Modifier.matchParentSize()) {
                val size = with(LocalDensity.current) { constraints.maxHeight.toDp() }
                val animationScope = rememberCoroutineScope()
                Box(
                    Modifier
                        .size(size, size)
                        .align(Alignment.CenterEnd)
                        .offset(x = size * 0.35f, y = -size * 0.35f)
                        .rotate(gearRotation.value)
                        .pointerInput(Unit) {
                            forEachGesture {
                                detectTapGestures {
                                    animationScope.launch {
                                        if (!gearRotation.isRunning) {
                                            gearRotation.animateTo(720f, tween(1000))
                                            gearRotation.snapTo(0f)
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = gear,
                        alpha = 0.12f,
                        colorFilter = ColorFilter.tint(contentColor),
                        contentDescription = null
                    )
                }
            }

            Column(Modifier.padding(16.dp)) {
                // Avatar
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clickable {
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
                    color = contentColor.copy(alpha = ContentAlpha.medium),
                    style = MaterialTheme.typography.subtitle1
                )
            }
        }
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
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
            Row(
                Modifier
                    .fillMaxSize()
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
                screens = MainViewModel.Screen.values(),
                currentScreen = MainViewModel.Screen.HOME,
                onChange = {}
            )
        }, items = {}, setting = {}) {}
    }
}
