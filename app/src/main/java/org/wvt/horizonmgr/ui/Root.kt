package org.wvt.horizonmgr.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@Composable
fun Root(
    viewModel: RootViewModel,
    onInstallHZClick: () -> Unit,
    onInstallMCClick: () -> Unit,
    onRequestPermission: () -> Unit,
    content: @Composable () -> Unit,
) {
    val showPermissionDialog by viewModel.showPermissionDialog.collectAsState()
    val newVersion by viewModel.newVersion.collectAsState()
    var displayNewVersionDialog by rememberSaveable { mutableStateOf(false) }

    val showGameNotInstall by viewModel.gameNotInstalled.collectAsState()
    val showHZNotInstall by viewModel.hzNotInstalled.collectAsState()

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
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            content()
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

        if (showHZNotInstall) {
            InstallHorizonDialog(
                onDismissClick = { viewModel.dismissHZNotInstallDialog() },
                onConfirmClick = {
                    onInstallHZClick()
                    viewModel.dismissHZNotInstallDialog()
                },
                onNeverShowClick = {
                    viewModel.neverShowHZInstallationTip()
                }
            )
        }

        if (showGameNotInstall) {
            InstallMCDialog(
                onDismissClick = { viewModel.dismissGameNotInstallDialog() },
                onConfirmClick = {
                    onInstallMCClick()
                    viewModel.dismissGameNotInstallDialog()
                },
                onNeverShowClick = {
                    viewModel.neverShowMCInstallationTip()
                }
            )
        }
    }
}

@Composable
private fun RequestPermissionDialog(
    onConfirm: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.shadow(16.dp, clip = false),
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
    AlertDialog(
        modifier = Modifier.shadow(16.dp, clip = false),
        title = { Text("发现新版本") },
        text = {
            Column(
                Modifier
                    .fillMaxHeight(0.6f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    """
                        |版本名：${versionName}
                        |版本号：${versionCode}
                        |更新日志：
                        |${changelog}
                        """.trimMargin()
                )
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