package org.wvt.horizonmgr.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.ui.platform.setContent
import org.wvt.horizonmgr.HorizonManagerApplication
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.community.CommunityActivity
import org.wvt.horizonmgr.ui.donate.DonateActivity
import org.wvt.horizonmgr.ui.fileselector.FileSelectorResult
import org.wvt.horizonmgr.ui.fileselector.FileSelectorResultContract
import org.wvt.horizonmgr.ui.joingroup.JoinGroupActivity
import org.wvt.horizonmgr.ui.login.LoginResultContract
import org.wvt.horizonmgr.ui.modulemanager.ModTabViewModel
import org.wvt.horizonmgr.ui.onlineinstall.InstallPackageResultContract
import org.wvt.horizonmgr.ui.pacakgemanager.PackageDetailActivity
import org.wvt.horizonmgr.ui.pacakgemanager.PackageManagerViewModel
import org.wvt.horizonmgr.ui.settings.SettingsActivity
import org.wvt.horizonmgr.ui.startActivity

class MainActivity : AppCompatActivity() {

    private val app: HorizonManagerApplication by lazy { application as HorizonManagerApplication }
    private val mainViewModel: MainViewModel by viewModels { app.dependenciesVMFactory }
    private val homeViewModel: HomeViewModel by viewModels { app.dependenciesVMFactory }
    private val modTabViewModel: ModTabViewModel by viewModels { app.dependenciesVMFactory }
    private val packageViewModel: PackageManagerViewModel by viewModels { app.dependenciesVMFactory }

    private val login = registerForActivityResult(LoginResultContract()) {
        mainViewModel.setUserInfo(it)
    }

    private val selectFileForPackage = registerForActivityResult(FileSelectorResultContract()) {
        if (it is FileSelectorResult.Succeed) {
            packageViewModel.selectedFile(it.filePath)
        }
    }

    private val selectFileForModule = registerForActivityResult(FileSelectorResultContract()) {
        if (it is FileSelectorResult.Succeed) {
            modTabViewModel.fileSelected(it.filePath)
        }
    }

    private val onlineInstall = registerForActivityResult(InstallPackageResultContract()) {
        packageViewModel.loadPackages()
    }

    private fun startDonateActivity() {
        startActivity<DonateActivity>()
    }

    private fun startJoinGroupActivity() {
        startActivity<JoinGroupActivity>()
    }

    private fun startCommunityActivity() {
        startActivity<CommunityActivity>()
    }

    private fun startSettingsActivity() {
        startActivity<SettingsActivity>()
    }

    private fun startLoginActivity() {
        login.launch(this)
    }

    private fun startSelectFileActivityForPackage() {
        selectFileForModule.launch(this)
    }

    private fun startSelectFileActivityForMod() {
        selectFileForPackage.launch(this)
    }

    private fun startOnlineInstallActivity() {
        onlineInstall.launch(this)
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

    private fun checkPermission() {
        fun check(): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return Environment.isExternalStorageManager()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                listOf<String>(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET
                ).forEach {
                    if (checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) return false
                }
            }
            return true
        }
        if (!check()) {
            mainViewModel.showPermissionDialog()
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
            this.requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET
                ), 0
            )
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_HorizonManagerCompose_NoActionBar) // cancel the slash theme

        setContent {
            App(
                viewModel = mainViewModel,
                homeViewModel = homeViewModel,
                onRequestPermission = { requestPermission() },
                navigateToCommunity = ::startCommunityActivity,
                navigateToDonate = ::startDonateActivity,
                navigateToJoinGroup = ::startJoinGroupActivity,
                navigateToLogin = ::startLoginActivity,
                navigateToSettings = ::startSettingsActivity,
                navigateToOnlineInstall = ::startOnlineInstallActivity,
                requestOpenGame = ::openGame,
                selectFileForMod = ::startSelectFileActivityForMod,
                selectFileForPackage = ::startSelectFileActivityForPackage,
                navigateToPackageInfo = { PackageDetailActivity.start(this, it) }
            )
        }

        this.checkPermission()
        mainViewModel.checkUpdate()
    }
}