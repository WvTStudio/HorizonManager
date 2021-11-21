package org.wvt.horizonmgr.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
/*
//    private val factory by lazy { HiltViewModelFactory(this, NavBackStackEntry.create(this)) }

    private val mainVM: MainViewModel by viewModels()
//    private val rootVM: RootViewModel by viewModels()
    private val homeVM: HomeViewModel by viewModels()
    private val modTabVM: ModTabViewModel by viewModels()
    private val icLevelTabVM: ICLevelTabViewModel by viewModels ()
    private val icResTabVM: ICResTabViewModel by viewModels()
    private val moduleManagerVM: ModuleManagerViewModel by viewModels()
    private val packageManagerVM: PackageManagerViewModel by viewModels()
    private val mcLevelVM: MCLevelTabViewModel by viewModels()
    private val downloadedModVM: DMViewModel by viewModels()
    private val onlineModsVM: OnlineModsViewModel by viewModels()
    private val mcResVM: MCResTabViewModel by viewModels()

    private val newsDetail = registerForActivityResult(ArticleContentActivityContract()) {}

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

    private val selectLevelForMC = registerForActivityResult(FileSelectorResultContract()) {
        if (it is FileSelectorResult.Succeed) {
            mcLevelVM.selectedFileToInstall(it.filePath)
        }
    }
    private val selectLevelForIC = registerForActivityResult(FileSelectorResultContract()) {
        if (it is FileSelectorResult.Succeed) {
            icLevelTabVM.selectedFileToInstall(it.filePath)
        }
    }

    private val selectTextureForIC = registerForActivityResult(FileSelectorResultContract()) {
        if (it is FileSelectorResult.Succeed) {
            icResTabVM.selectedFileToInstall(it.filePath)
        }
    }
    private val selectTextureForMC = registerForActivityResult(FileSelectorResultContract()) {
        if (it is FileSelectorResult.Succeed) {
            mcResVM.selectedFileToInstall(it.filePath)
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

    private fun startSelectFileActivityForPackage() {
        selectFileForPackage.launch(this)
    }

    private fun startSelectFileActivityForMod() {
        selectFileForModule.launch(this)
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
            mainVM.showGameNotInstallDialog()
        }
        if (!hasHZ) {
            mainVM.showHZNotInstallDialog()
        }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_HorizonManagerCompose_NoActionBar) // cancel the slash theme

        setContent {
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Root(
                        onRequestPermission = ::requestPermission,
                        navigateToCommunity = ::startCommunityActivity,
                        navigateToDonate = ::startDonateActivity,
                        navigateToJoinGroup = ::startJoinGroupActivity,
                        navigateToLogin = { login.launch(Unit) },
                        navigateToSettings = ::startSettingsActivity,
                        navigateToOnlineInstall = ::startOnlineInstallActivity,
                        navigateToPackageInfo = { PackageDetailActivity.start(this, it) },
                        requestOpenGame = ::openGame,
                        selectFileForMod = ::startSelectFileActivityForMod,
                        selectFileForPackage = ::startSelectFileActivityForPackage,
                        selectLevelForIC = { selectLevelForIC.launch(this) },
                        selectLevelForMC = { selectLevelForMC.launch(this) },
                        selectTextureForIC = { selectTextureForIC.launch(this) },
                        selectTextureForMC = { selectTextureForMC.launch(this) },
                        onInstallHZClick = { openCoolapkURL() },
                        onInstallMCClick = { openMCGooglePlay() },
                        navigateToNewsDetail = { newsDetail.launch(it) }
                    )
                }
            }
        }

        this.checkPermission()
        mainVM.checkUpdate()
        checkGameInstalled()
    }*/
}