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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R
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
        confirmButton = { TextButton(onClick = onConfirm) { Text(text = stringResource(R.string.permission_dialog_confirm)) } },
        title = { Text(stringResource(R.string.permission_dialog_title)) },
        text = { Text(stringResource(R.string.permission_dialog_text)) }
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
        title = { Text(stringResource(R.string.update_dialog_title)) },
        text = {
            Column(
                Modifier
                    .fillMaxHeight(0.6f)
                    .verticalScroll(rememberScrollState())
            ) {
                val vnLabel = stringResource(R.string.update_dialog_label_versionname)
                val vcLabel = stringResource(R.string.update_dialog_label_versioncode)
                val cLabel = stringResource(R.string.update_dialog_label_changelog)

                Text(
                    """
                    |$vnLabel：${versionName}
                    |$vcLabel：${versionCode}
                    |$cLabel：
                    |${changelog}
                    """.trimMargin()
                )
            }
        },
        onDismissRequest = onConfirm,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.update_dialog_action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onIgnore) {
                Text(stringResource(R.string.update_dialog_action_ignore))
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