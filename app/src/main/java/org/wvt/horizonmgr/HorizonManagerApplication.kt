package org.wvt.horizonmgr

import android.app.Application
import android.os.Build
import android.os.Environment
import dalvik.system.ZipPathValidator
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.wvt.horizonmgr.service.hzpack.HorizonManager
import org.wvt.horizonmgr.service.level.LevelTransporter
import org.wvt.horizonmgr.service.level.MCLevelManager
import org.wvt.horizonmgr.service.respack.ResourcePackManager
import org.wvt.horizonmgr.ui.RootViewModel
import org.wvt.horizonmgr.ui.theme.AndroidThemeController
import org.wvt.horizonmgr.utils.LocalCache
import org.wvt.horizonmgr.utils.ModDownloader
import org.wvt.horizonmgr.utils.OfficialCDNPackageDownloader
import org.wvt.horizonmgr.viewmodel.ArticleContentViewModel
import org.wvt.horizonmgr.viewmodel.CommunityViewModel
import org.wvt.horizonmgr.viewmodel.CustomThemeViewModel
import org.wvt.horizonmgr.viewmodel.DMViewModel
import org.wvt.horizonmgr.viewmodel.DonateViewModel
import org.wvt.horizonmgr.viewmodel.DownloadListViewModel
import org.wvt.horizonmgr.viewmodel.FileSelectorViewModel
import org.wvt.horizonmgr.viewmodel.HomeViewModel
import org.wvt.horizonmgr.viewmodel.ICLevelTabViewModel
import org.wvt.horizonmgr.viewmodel.ICResTabViewModel
import org.wvt.horizonmgr.viewmodel.InstallPackageViewModel
import org.wvt.horizonmgr.viewmodel.JoinGroupViewModel
import org.wvt.horizonmgr.viewmodel.LoginViewModel
import org.wvt.horizonmgr.viewmodel.MCLevelTabViewModel
import org.wvt.horizonmgr.viewmodel.MainViewModel
import org.wvt.horizonmgr.viewmodel.ModTabViewModel
import org.wvt.horizonmgr.viewmodel.ModuleManagerViewModel
import org.wvt.horizonmgr.viewmodel.OnlineModDetailViewModel
import org.wvt.horizonmgr.viewmodel.OnlineModsViewModel
import org.wvt.horizonmgr.viewmodel.PackageDetailViewModel
import org.wvt.horizonmgr.viewmodel.PackageManagerViewModel
import org.wvt.horizonmgr.viewmodel.SharedFileChooserViewModel
import org.wvt.horizonmgr.webapi.iccn.ICCNModule
import org.wvt.horizonmgr.webapi.mgrinfo.MgrInfoModule
import org.wvt.horizonmgr.webapi.mod.ChineseModRepository
import org.wvt.horizonmgr.webapi.mod.OfficialModMirrorRepository
import org.wvt.horizonmgr.webapi.news.MgrArticleModule
import org.wvt.horizonmgr.webapi.pack.OfficialPackageCDNRepository

class HorizonManagerApplication : Application() {
    lateinit var themeController: AndroidThemeController
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Enable Zip path traversal
            ZipPathValidator.clearCallback()
        }
        startKoin {
            androidLogger()
            androidContext(this@HorizonManagerApplication)
            modules(myAppModules)
        }

        themeController = AndroidThemeController(this)
    }
}

val myAppModules = module {
    single { LocalCache(androidApplication()) }
    single {
        HorizonManager(
            Environment.getExternalStorageDirectory().resolve("games").resolve("horizon")
        )
    }
    single { OfficialPackageCDNRepository() }
    single { ChineseModRepository() }
    single { OfficialModMirrorRepository() }
    single { MgrInfoModule() }
    single { ICCNModule() }
    single { MgrArticleModule() }
    single { OfficialCDNPackageDownloader(androidApplication()) }
    single { ModDownloader(androidApplication()) }
    single {
        MCLevelManager(
            Environment.getExternalStorageDirectory()
                .resolve("games")
                .resolve("com.mojang")
                .resolve("minecraftWorlds")
        )
    }
    single {
        LevelTransporter(
            Environment.getExternalStorageDirectory()
                .resolve("games")
                .resolve("com.mojang")
                .resolve("minecraftWorlds").absolutePath
        )
    }
    single {
        ResourcePackManager(
            Environment.getExternalStorageDirectory()
                .resolve("games")
                .resolve("com.mojang")
                .resolve("resource_packs")
        )
    }

    singleOf(::RootViewModel)

    viewModelOf(::ArticleContentViewModel)
    viewModelOf(::CommunityViewModel)
    viewModelOf(::CustomThemeViewModel)
    viewModelOf(::DMViewModel)
    viewModelOf(::DonateViewModel)
    viewModelOf(::DownloadListViewModel)
    viewModelOf(::FileSelectorViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ICLevelTabViewModel)
    viewModelOf(::ICResTabViewModel)
    viewModelOf(::InstallPackageViewModel)
    viewModelOf(::JoinGroupViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::MCLevelTabViewModel)
    viewModelOf(::ModTabViewModel)
    viewModelOf(::ModuleManagerViewModel)
    viewModelOf(::OnlineModDetailViewModel)
    viewModelOf(::OnlineModsViewModel)
    viewModelOf(::PackageDetailViewModel)
    viewModelOf(::PackageManagerViewModel)
}