package org.wvt.horizonmgr.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.HorizonManagerApplication
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.AndroidDependenciesProvider
import org.wvt.horizonmgr.ui.community.CommunityActivity
import org.wvt.horizonmgr.ui.donate.DonateActivity
import org.wvt.horizonmgr.ui.joingroup.JoinGroupActivity
import org.wvt.horizonmgr.ui.settings.SettingsActivity
import org.wvt.horizonmgr.ui.startActivity
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.ui.theme.PreviewTheme

class MainActivity : AppCompatActivity() {
    private val dependencies = HorizonManagerApplication.container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_HorizonManagerCompose_NoActionBar) // cancel the slash theme
        setContent {
            val vm = dependenciesViewModel<MainActivityViewModel>()
            val userInfo by vm.userInfo.collectAsState()
            val selectedPackage by vm.selectedPackage.collectAsState()
            val showPermissionDialog by vm.showPermissionDialog.collectAsState()

            val newVersion by vm.newVersion.collectAsState()
            var displayNewVersionDialog by savedInstanceState { false }

//            val navController = rememberNavController()

            onCommit(newVersion) {
                if (newVersion != null) {
                    displayNewVersionDialog = true
                }
            }

            onActive { vm.checkPermission(this@MainActivity) }

            AndroidDependenciesProvider {
                AndroidHorizonManagerTheme {
                    Surface(color = MaterialTheme.colors.background) {
                        App(
//                            navController = navController,
                            dependencies = dependencies,
                            userInfo = userInfo,
                            requestLogin = { vm.requestLogin(this) },
                            requestLogout = vm::logOut,
                            selectedPackageUUID = selectedPackage,
                            selectedPackageChange = vm::setSelectedPackage,
                            openGame = { vm.openGame(this) },
                            community = { startActivity<CommunityActivity>() },
                            joinGroup = { startActivity<JoinGroupActivity>() },
                            donate = { startActivity<DonateActivity>() },
                            settings = { startActivity<SettingsActivity>() },
                        )

                        if (showPermissionDialog) {
                            RequestPermissionDialog {
                                vm.dismiss()
                                vm.requestPermission(this)
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
                                    vm.ignoreVersion(theNewVersion.versionCode)
                                    displayNewVersionDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestPermissionDialog(
    onConfirm: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.shadow(24.dp, shape = RoundedCornerShape(4.dp)),
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
        modifier = Modifier.shadow(24.dp, shape = RoundedCornerShape(4.dp)),
        title = { Text("发现新版本") },
        text = {
            Text(
                """
                    版本名：${versionName}
                    版本号：${versionCode}
                    更新日志：${changelog}
                """.trimIndent()
            )
        },
        onDismissRequest = {},
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
                changelog = "测试更新日志",
                onConfirm = {},
                onIgnore = {}
            )
        }
    }
}