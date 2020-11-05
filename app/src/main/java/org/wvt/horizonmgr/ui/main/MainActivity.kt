package org.wvt.horizonmgr.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.platform.setContent
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

            onCommit(newVersion) {
                if (newVersion != null) {
                    displayNewVersionDialog = true
                }
            }

            onActive {
                vm.checkPermission(this@MainActivity)
            }

            AndroidDependenciesProvider {
                AndroidHorizonManagerTheme {
                    Surface(color = MaterialTheme.colors.background) {
                        App(
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
                            AlertDialog(
                                onDismissRequest = { },
                                confirmButton = {
                                    TextButton(onClick = {
                                        vm.dismiss()
                                        vm.requestPermission(this)
                                    }) { Text(text = "确定") }
                                },
                                title = { Text("需要权限") },
                                text = {
                                    Text("本应用需要拥有网络权限及对内置存储的完全访问权限。\n如果您的系统版本为 Android R 及以上，您需要在弹出的系统设置中授予完全访问权限")
                                }
                            )
                        }

                        val theNewVersion = newVersion

                        if (theNewVersion != null && displayNewVersionDialog) {
                            AlertDialog(
                                title = { Text("发现新版本") },
                                text = {
                                    Text(
                                        "版本名：${theNewVersion.versionName}\n" +
                                                "版本号：${theNewVersion.versionCode}\n" +
                                                "更新日志：${theNewVersion.changelog}"
                                    )
                                },
                                onDismissRequest = {},
                                confirmButton = {
                                    TextButton(onClick = { displayNewVersionDialog = false }) {
                                        Text("确定")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        vm.ignoreVersion(theNewVersion.versionCode)
                                        displayNewVersionDialog = false
                                    }) {
                                        Text("忽略该版本")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}