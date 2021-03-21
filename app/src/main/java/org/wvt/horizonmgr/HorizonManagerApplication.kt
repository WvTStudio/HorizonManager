package org.wvt.horizonmgr

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.wvt.horizonmgr.service.hzpack.HorizonManager
import org.wvt.horizonmgr.service.level.LevelTransporter
import org.wvt.horizonmgr.service.level.MCLevelManager
import org.wvt.horizonmgr.service.respack.ResourcePackManager
import org.wvt.horizonmgr.utils.LocalCache
import org.wvt.horizonmgr.utils.ModDownloader
import org.wvt.horizonmgr.utils.OfficialCDNPackageDownloader
import org.wvt.horizonmgr.webapi.iccn.ICCNModule
import org.wvt.horizonmgr.webapi.mgrinfo.MgrInfoModule
import org.wvt.horizonmgr.webapi.mod.ChineseModRepository
import org.wvt.horizonmgr.webapi.mod.OfficialModMirrorRepository
import org.wvt.horizonmgr.webapi.news.MgrNewsModule
import org.wvt.horizonmgr.webapi.pack.OfficialPackageCDNRepository

class HorizonManagerApplication : Application() {
    private lateinit var container: DependenciesContainer
    lateinit var dependenciesVMFactory: ViewModelProvider.Factory
        private set

    override fun onCreate() {
        super.onCreate()
        container = DependenciesContainer(this)
        dependenciesVMFactory = DependenciesVMFactory(container)
    }
}

val Context.defaultViewModelFactory: ViewModelProvider.Factory
    get() = (applicationContext as HorizonManagerApplication).dependenciesVMFactory

private class DependenciesVMFactory(
    private val dependenciesContainer: DependenciesContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(DependenciesContainer::class.java)
            .newInstance(dependenciesContainer)
    }
}

class DependenciesContainer internal constructor(private val context: Context) {
    val localCache by lazy { LocalCache(context) }
    val manager by lazy {
        HorizonManager(
            Environment.getExternalStorageDirectory().resolve("games").resolve("horizon")
        )
    }
    val packRepository by lazy { OfficialPackageCDNRepository() }
    val chineseModRepository by lazy { ChineseModRepository() }
    val mirrorModRepository by lazy { OfficialModMirrorRepository() }
    val mgrInfo by lazy { MgrInfoModule() }
    val iccn by lazy { ICCNModule() }
    val news by lazy { MgrNewsModule() }
    val packageDownloader by lazy { OfficialCDNPackageDownloader(context) }
    val modDownloader by lazy { ModDownloader(context) }
    val mcLevelManager by lazy {
        MCLevelManager(
            Environment.getExternalStorageDirectory()
                .resolve("games")
                .resolve("com.mojang")
                .resolve("minecraftWorlds")
        )
    }
    val levelTransporter by lazy {
        LevelTransporter(
            Environment.getExternalStorageDirectory()
                .resolve("games")
                .resolve("com.mojang")
                .resolve("minecraftWorlds").absolutePath
        )
    }

    val mcResourcePackManager by lazy {
        ResourcePackManager(
            Environment.getExternalStorageDirectory()
                .resolve("games")
                .resolve("com.mojang")
                .resolve("resource_packs")
        )
    }
}
