package org.wvt.horizonmgr.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.joingroup.JoinGroupViewModel
import javax.inject.Inject

@AndroidEntryPoint
class RootActivity : AppCompatActivity() {

    @Inject protected lateinit var rootVM: RootViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_HorizonManagerCompose_NoActionBar) // cancel the slash theme
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
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.coolapk.com/game/com.zheka.horizon"))
        startActivity(intent)
    }

    private fun openMCGooglePlay() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=com.mojang.minecraftpe")
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

    val joinGroupVM by viewModels<JoinGroupViewModel>()

    private fun openURL(url: String) {
        try {
            val intent = Intent()
            intent.data = Uri.parse(url)
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
        // TODO: 2020/10/13 支持挂起，在用户完成操作后恢复
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && Build.VERSION.PREVIEW_SDK_INT != 0) {
            // R Preview
            @SuppressLint("NewApi")
            if (!Environment.isExternalStorageManager()) this.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) this.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(permissions, 0)
        }
    }

    @SuppressLint("QueryPermissionsNeeded", "WrongConstant")
    private fun checkGameInstalled() {
        val apps = packageManager.getInstalledApplications(PackageManager.GET_ACTIVITIES)
        var hasMC = false
        var hasHZ = false

        for (app in apps) {
            when (app.packageName) {
                "com.mojang.minecraftpe" -> hasMC = true
                "com.zheka.horizon" -> hasHZ = true
            }
        }

        if (!hasMC) {
            rootVM.showGameNotInstallDialog()
        }
        if (!hasHZ) {
            rootVM.showHZNotInstallDialog()
        }
    }
}