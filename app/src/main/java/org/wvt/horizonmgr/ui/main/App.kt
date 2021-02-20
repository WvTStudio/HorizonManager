package org.wvt.horizonmgr.ui.main

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.wvt.horizonmgr.ui.components.MyAlertDialog
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@Composable
fun App(
    viewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    onRequestPermission: () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToJoinGroup: () -> Unit,
    navigateToCommunity: () -> Unit,
    navigateToDonate: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToOnlineInstall: () -> Unit,
    navigateToPackageInfo: (uuid: String) -> Unit,
    requestOpenGame: () -> Unit,
    selectFileForMod: () -> Unit,
    selectFileForPackage: () -> Unit
) {
    val userInfo by viewModel.userInfo.collectAsState()
    val selectedPackage by viewModel.selectedPackage.collectAsState()
    val showPermissionDialog by viewModel.showPermissionDialog.collectAsState()

    val newVersion by viewModel.newVersion.collectAsState()
    var displayNewVersionDialog by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(newVersion) {
        if (newVersion != null) {
            displayNewVersionDialog = true
        }
        onDispose {}
    }

    DisposableEffect(Unit) {
        viewModel.checkUpdate()
        onDispose {
            // TODO: 2021/2/6 添加 Cancel 逻辑
        }
    }

    AndroidHorizonManagerTheme {
        Surface(color = MaterialTheme.colors.background) {
            if (viewModel.initialized) Home(
                viewModel = homeViewModel,
                userInfo = remember(userInfo) {
                    userInfo?.let {
                        UserInformation(
                            it.name,
                            it.account,
                            it.avatarUrl
                        )
                    }
                },
                requestLogin = navigateToLogin,
                requestLogout = viewModel::logOut,
                selectedPackageUUID = selectedPackage,
                selectedPackageChange = viewModel::setSelectedPackage,
                openGame = requestOpenGame,
                community = navigateToCommunity,
                joinGroup = navigateToJoinGroup,
                donate = navigateToDonate,
                settings = navigateToSettings,
                requestOnlineInstall = navigateToOnlineInstall,
                onAddModClicked = selectFileForMod,
                onAddPackageClicked = selectFileForPackage,
                navigateToPackageInfo = navigateToPackageInfo
            )
        }

        if (showPermissionDialog) {
            RequestPermissionDialog {
                viewModel.dismiss()
                onRequestPermission()
            }
        }

        val theNewVersion = newVersion

        if (theNewVersion != null && displayNewVersionDialog) {
            NewVersionDialog(
                versionName = theNewVersion.versionName,
                versionCode = theNewVersion.versionCode,
                changelog = theNewVersion.changelog,
                onConfirm = { displayNewVersionDialog = false },
                onIgnore = {
                    viewModel.ignoreVersion(theNewVersion.versionCode)
                    displayNewVersionDialog = false
                }
            )
        }
    }
}

@Composable
private fun RequestPermissionDialog(
    onConfirm: () -> Unit
) {
    MyAlertDialog(
        onDismissRequest = { },
        confirmButton = { TextButton(onClick = onConfirm) { Text(text = "授权") } },
        title = { Text("需要权限") },
        text = { Text("本应用需要拥有网络权限及对内置存储的完全访问权限。\n如果您的系统版本为 Android R 及以上，您需要在弹出的系统设置中授予完全访问权限") }
    )
}

@Composable
private fun NewVersionDialog(
    versionName: String,
    versionCode: Int,
    changelog: String,
    onConfirm: () -> Unit,
    onIgnore: () -> Unit
) {
    MyAlertDialog(
        title = { Text("发现新版本") },
        text = {
            LazyColumn(Modifier.fillMaxHeight(0.6f)) {
                item {
                    Text(
                        """
                        |版本名：${versionName}
                        |版本号：${versionCode}
                        |更新日志：
                        |${changelog}
                        """.trimMargin()
                    )
                }
            }
        },
        onDismissRequest = onConfirm,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onIgnore) {
                Text("忽略该版本")
            }
        }
    )
}

@Preview
@Composable
private fun NewVersionDialogPreview() {
    PreviewTheme {
        Surface(Modifier.fillMaxSize()) {
            NewVersionDialog(
                versionName = "2.0.0",
                versionCode = 100,
                changelog = "test",
                onConfirm = {},
                onIgnore = {}
            )
        }
    }
}