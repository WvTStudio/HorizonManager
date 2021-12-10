package org.wvt.horizonmgr.ui.main

import androidx.compose.animation.*
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.downloaded.DownloadedMods
import org.wvt.horizonmgr.ui.home.HomeScreen
import org.wvt.horizonmgr.ui.modulemanager.ModuleManagerScreen
import org.wvt.horizonmgr.ui.onlinemods.OnlineMods
import org.wvt.horizonmgr.ui.pacakgemanager.PackageManagerScreen
import org.wvt.horizonmgr.ui.theme.LocalThemeConfig
import org.wvt.horizonmgr.viewmodel.HomeViewModel
import org.wvt.horizonmgr.viewmodel.MainViewModel

enum class Screen {
    HOME, PACKAGE_MANAGE, LOCAL_MANAGE, ONLINE_DOWNLOAD, DOWNLOADED_MOD
}

@OptIn(ExperimentalAnimationApi::class)
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
    val userInfo by viewModel.userInfo.collectAsState()

    var screen by rememberSaveable { mutableStateOf(Screen.PACKAGE_MANAGE) }

    LocalLifecycleOwner.current.lifecycleScope.launchWhenResumed {
        viewModel.resume()
    }

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
            DrawerTabs(screens = remember { Screen.values() }, currentScreen = screen, onChange = {
                scope.launch {
                    delay(100)
                    drawerState.close()
                    screen = it
                }
            })
        },
        items = {
            NavigationItem(
                checked = false,
                onCheckedChange = { navigateToCommunity() },
                text = stringResource(id = R.string.main_screen_item_label_community),
                icon = Icons.Rounded.Forum
            )
            NavigationItem(
                checked = false,
                onCheckedChange = { requestOpenGame() },
                text = stringResource(id = R.string.main_screen_item_label_opengame),
                icon = Icons.Rounded.Gamepad
            )
            NavigationItem(
                checked = false,
                onCheckedChange = { navigateToJoinGroup() },
                text = stringResource(id = R.string.main_screen_item_label_joingroup),
                icon = Icons.Rounded.Group
            )
            NavigationItem(
                checked = false,
                onCheckedChange = { navigateToDonate() },
                text = stringResource(id = R.string.main_screen_item_label_donate),
                icon = Icons.Rounded.AttachMoney
            )
        },
        setting = {
            NavigationItem(
                checked = false,
                onCheckedChange = { navigateToSettings() },
                text = stringResource(id = R.string.main_screen_item_label_settings),
                icon = Icons.Rounded.Settings
            )
        }
    ) {
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = screen,
            transitionSpec = {
                ContentTransform(targetContentEnter = fadeIn(), initialContentExit = fadeOut())
            }
        ) { cs ->
            when (cs) {
                Screen.HOME -> HomeScreen(
                    onNavClick = { scope.launch { drawerState.open() } },
                    onNewsClick = {
                        if (it is HomeViewModel.ContentResource.Article) {
                            navigateToArticleDetail(it.id)
                        }
                    }
                )
                Screen.LOCAL_MANAGE -> ModuleManagerScreen(
                    onNavClicked = { scope.launch { drawerState.open() } },
                    onAddModClicked = onAddModClicked,
                    onAddICLevelClick = onAddICLevelClick,
                    onAddICTextureClick = onAddICTextureClick,
                    onAddMCLevelClick = onAddMCLevelClick,
                    onAddMCTextureClick = onAddMCTextureClick
                )
                Screen.PACKAGE_MANAGE -> PackageManagerScreen(
                    onNavClick = { scope.launch { drawerState.open() } },
                    onOnlineInstallClick = navigateToOnlineInstall,
                    onLocalInstallClick = onAddPackageClicked,
                    navigateToPackageInfo = navigateToPackageDetail
                )
                Screen.ONLINE_DOWNLOAD -> OnlineMods(
                    viewModel = hiltViewModel(),
                    isLogon = userInfo != null,
                    onNavClick = { scope.launch { drawerState.open() } }
                )
                Screen.DOWNLOADED_MOD -> DownloadedMods(
                    vm = hiltViewModel(),
                    onNavClicked = { scope.launch { drawerState.open() } }
                )
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

@Composable
private fun DrawerTabs(
    screens: Array<Screen>,
    currentScreen: Screen,
    onChange: (Screen) -> Unit,
) {
    screens.forEach {
        NavigationItem(
            checked = currentScreen == it,
            onCheckedChange = { checked -> if (checked) onChange(it) },
            screen = it
        )
    }
}

@Composable
private fun NavigationItem(screen: Screen, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val text: String
    val icon: ImageVector

    when (screen) {
        Screen.HOME -> {
            text = stringResource(id = R.string.main_screen_nav_label_home)
            icon = Icons.Rounded.Home
        }
        Screen.PACKAGE_MANAGE -> {
            text = stringResource(id = R.string.main_screen_nav_label_package_manage)
            icon = Icons.Rounded.Dashboard
        }
        Screen.LOCAL_MANAGE -> {
            text = stringResource(id = R.string.main_screen_nav_label_local_manage)
            icon = Icons.Rounded.Extension
        }
        Screen.ONLINE_DOWNLOAD -> {
            text = stringResource(id = R.string.main_screen_nav_label_online_download)
            icon = Icons.Rounded.Store
        }
        Screen.DOWNLOADED_MOD -> {
            text = stringResource(id = R.string.main_screen_nav_label_downlaoded_mod)
            icon = Icons.Rounded.Storage
        }
    }

    NavigationItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        text = text,
        icon = icon
    )
}

data class UserInformation(
    val name: String,
    val account: String,
    val avatarUrl: String
)

@Composable
private fun LogoutAlertDialog(
    onDismissRequest: () -> Unit,
    onLogoutClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.shadow(16.dp, clip = false),
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onLogoutClick) {
                Text(stringResource(id = R.string.logout_alert_dialog_logout))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelClick) { Text(stringResource(id = R.string.logout_alert_dialog_cancel)) }
        }, title = {
            Text(stringResource(id = R.string.logout_alert_dialog_title))
        }, text = {
            Text(stringResource(id = R.string.logout_alert_dialog_text))
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DrawerHeader(
    userInfo: UserInformation?,
    requestLogin: () -> Unit,
    requestLogout: () -> Unit
) {
    val theme = LocalThemeConfig.current
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        LogoutAlertDialog(
            onDismissRequest = { showDialog = false },
            onLogoutClick = {
                requestLogout()
                showDialog = false
            },
            onCancelClick = {
                showDialog = false
            }
        )
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = theme.appbarColor
    ) {
        Box {
            BoxWithConstraints(Modifier.matchParentSize()) {
                val size = with(LocalDensity.current) { constraints.maxHeight.toDp() }
                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(size, size)
                        .offset(x = size * 0.35f, y = -size * 0.35f)
                ) { AnimationGear() }
            }
            Column(Modifier.padding(16.dp)) {
                // Avatar
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                    onClick = {
                        if (userInfo == null) requestLogin()
                        else showDialog = true
                    }
                ) {
                    Box(Modifier.fillMaxSize()) {
                        userInfo?.let {
                            NetworkImage(
                                url = it.avatarUrl,
                                contentDescription = stringResource(id = R.string.main_screen_drawer_avatar_description),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                Text(
                    if (userInfo == null) stringResource(id = R.string.main_screen_drawer_logintip) else
                        stringResource(id = R.string.main_screen_drawer_welcome).replace(
                            "\${username}",
                            userInfo.name
                        ),
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.h5
                )
                Text(
                    userInfo?.account ?: stringResource(id = R.string.main_screen_drawer_banner),
                    modifier = Modifier.padding(top = 8.dp),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    style = MaterialTheme.typography.subtitle1
                )
            }
        }
    }
}

@Composable
private fun AnimationGear() {
    val gear = painterResource(id = R.drawable.ic_gear_full)
    val gearRotation = remember { Animatable(0f) }
    val animationScope = rememberCoroutineScope()
    Box(
        Modifier
            .fillMaxSize()
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
            colorFilter = ColorFilter.tint(LocalContentColor.current),
            contentDescription = null
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
        Row(
            Modifier
                .fillMaxSize()
                .clickable { onCheckedChange(!checked) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                modifier = Modifier.padding(start = 8.dp),
                contentDescription = null,
                tint = LocalContentColor.current.copy(if (checked) ContentAlpha.high else ContentAlpha.medium)
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 24.dp, end = 8.dp),
                fontWeight = FontWeight.Medium,
                color = LocalContentColor.current.copy(ContentAlpha.high)
            )
        }
    }
}