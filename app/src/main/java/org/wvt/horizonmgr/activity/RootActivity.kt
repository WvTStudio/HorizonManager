package org.wvt.horizonmgr.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.net.toUri
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.Root
import org.wvt.horizonmgr.ui.RootNavHostActivity
import org.wvt.horizonmgr.ui.RootViewModel
import org.wvt.horizonmgr.viewmodel.JoinGroupViewModel

class RootActivity : ComponentActivity() {

    val rootVM: RootViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_HorizonManagerCompose_NoActionBar) // cancel the splash theme
        setContent {
            Root(
                viewModel = rootVM,
                onInstallHZClick = this::openCoolapkURL,
                onInstallMCClick = this::openMCGooglePlay,
                onRequestPermission = this::requestPermission,
            ) {
                RootNavHostActivity(
                    requestOpenGame = this::openGame,
                    requestOpenURL = this::openURL
                )
            }
        }
        this.checkPermission()
        this.checkGameInstalled()
        rootVM.checkUpdate()
    }

    private fun openCoolapkURL() {
        val intent =
            Intent(Intent.ACTION_VIEW, "https://www.coolapk.com/game/com.zheka.horizon".toUri())
        startActivity(intent)
    }

    private fun openMCGooglePlay() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "https://play.google.com/store/apps/details?id=com.mojang.minecraftpe".toUri()
        )
        startActivity(intent)
    }

    private fun openGame() {
        try {
            val horizonIntent =
                Intent(packageManager.getLaunchIntentForPackage("com.zheka.horizon"))
            startActivity(horizonIntent)
            return
        } catch (e: Exception) {
        }

        try {
            val innerCoreIntent =
                Intent(packageManager.getLaunchIntentForPackage("com.zheka.innercore"))
            startActivity(innerCoreIntent)
            return
        } catch (e: Exception) {
        }

        // TODO: 2021/2/8 全都打开失败后提示
    }

    val joinGroupVM by viewModel<JoinGroupViewModel>()

    private fun openURL(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        } catch (e: Exception) {
            joinGroupVM.startQQFailed()
        }
    }

    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.INTERNET
    )

    private fun checkPermission() {
        fun check(): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return Environment.isExternalStorageManager()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissions.forEach {
                    if (checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) return false
                }
            }
            return true
        }
        if (!check()) {
            rootVM.showPermissionDialog()
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) this.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(permissions, 0)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun checkGameInstalled() {
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            packageManager.getInstalledApplications(0)
        }
        val apps = packages.asSequence().map { it.packageName }.toSet()

        if ("com.mojang.minecraftpe" !in apps) {
            rootVM.showGameNotInstallDialog()
        }
        if ("com.zheka.horizon" !in apps && "com.zheka.horizon64" !in apps) {
            rootVM.showHZNotInstallDialog()
        }
    }
}