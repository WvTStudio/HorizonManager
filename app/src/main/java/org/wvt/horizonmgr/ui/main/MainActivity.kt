package org.wvt.horizonmgr.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.defaultViewModelFactory
import org.wvt.horizonmgr.ui.community.CommunityActivity
import org.wvt.horizonmgr.ui.donate.DonateActivity
import org.wvt.horizonmgr.ui.downloaded.DMViewModel
import org.wvt.horizonmgr.ui.fileselector.FileSelectorResult
import org.wvt.horizonmgr.ui.fileselector.FileSelectorResultContract
import org.wvt.horizonmgr.ui.joingroup.JoinGroupActivity
import org.wvt.horizonmgr.ui.login.LoginResultContract
import org.wvt.horizonmgr.ui.modulemanager.ICLevelTabViewModel
import org.wvt.horizonmgr.ui.modulemanager.MCLevelTabViewModel
import org.wvt.horizonmgr.ui.modulemanager.ModTabViewModel
import org.wvt.horizonmgr.ui.modulemanager.ModuleManagerViewModel
import org.wvt.horizonmgr.ui.news.NewsViewModel
import org.wvt.horizonmgr.ui.onlineinstall.InstallPackageResultContract
import org.wvt.horizonmgr.ui.onlinemods.OnlineViewModel
import org.wvt.horizonmgr.ui.pacakgemanager.PackageDetailActivity
import org.wvt.horizonmgr.ui.pacakgemanager.PackageManagerViewModel
import org.wvt.horizonmgr.ui.settings.SettingsActivity
import org.wvt.horizonmgr.ui.startActivity

class MainActivity : AppCompatActivity() {
    private val factory by lazy { defaultViewModelFactory }

    private val mainVM: MainViewModel by viewModels { factory }
    private val homeVM: HomeViewModel by viewModels { factory }
    private val newsVM: NewsViewModel by viewModels { factory }
    private val modTabVM: ModTabViewModel by viewModels { factory }
    private val icLevelTabVM: ICLevelTabViewModel by viewModels { factory }
    private val moduleManagerVM: ModuleManagerViewModel by viewModels { factory }
    private val packageManagerVM: PackageManagerViewModel by viewModels { factory }
    private val mcLevelVM: MCLevelTabViewModel by viewModels { factory }
    private val downloadedModVM: DMViewModel by viewModels { factory }
    private val onlineVM: OnlineViewModel by viewModels { factory }

    private val login = registerForActivityResult(LoginResultContract()) {
        mainVM.setUserInfo(it)
    }

    private val selectFileForPackage = registerForActivityResult(FileSelectorResultContract()) {
        if (it is FileSelectorResult.Succeed) {
            packageManagerVM.selectedFile(it.filePath)
        }
    }

    private val selectFileForModule = registerForActivityResult(FileSelectorResultContract()) {
        if (it is FileSelectorResult.Succeed) {
            modTabVM.fileSelected(it.filePath)
        }
    }

    private val onlineInstall =
        registerForActivityResult(InstallPackageResultContract()) { packageManagerVM.loadPackages() }

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
            mainVM.showPermissionDialog()
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
                mainVM = mainVM,
                homeVM = homeVM,
                newsVM = newsVM,
                modTabVM = modTabVM,
                icLevelTabVM = icLevelTabVM,
                moduleManagerVM = moduleManagerVM,
                packageManagerVM = packageManagerVM,
                mcLevelVM = mcLevelVM,
                downloadedModVM = downloadedModVM,
                onlineVM = onlineVM,
                onRequestPermission = ::requestPermission,
                navigateToCommunity = ::startCommunityActivity,
                navigateToDonate = ::startDonateActivity,
                navigateToJoinGroup = ::startJoinGroupActivity,
                navigateToLogin = ::startLoginActivity,
                navigateToSettings = ::startSettingsActivity,
                navigateToOnlineInstall = ::startOnlineInstallActivity,
                navigateToPackageInfo = { PackageDetailActivity.start(this, it) },
                requestOpenGame = ::openGame,
                selectFileForMod = ::startSelectFileActivityForMod,
                selectFileForPackage = ::startSelectFileActivityForPackage
            )
        }

        this.checkPermission()
        mainVM.checkUpdate()
    }
}