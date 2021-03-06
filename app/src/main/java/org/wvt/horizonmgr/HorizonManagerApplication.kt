package org.wvt.horizonmgr

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.wvt.horizonmgr.legacyservice.HorizonManager
import org.wvt.horizonmgr.legacyservice.LocalCache
import org.wvt.horizonmgr.service.level.LevelTransporter
import org.wvt.horizonmgr.service.level.MCLevelManager
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
    @Deprecated("Deprecated")
    val horizonManager: HorizonManager by lazy {
        HorizonManager.getOrCreateInstance(context)
    }

    val localCache: LocalCache by lazy {
        LocalCache.createInstance(context)
    }

    val manager by lazy {
        org.wvt.horizonmgr.service.HorizonManager(context)
    }

    val packRepository by lazy {
        OfficialPackageCDNRepository()
    }

    val chineseModRepository by lazy {
        ChineseModRepository()
    }

    val mirrorModRepository by lazy {
        OfficialModMirrorRepository()
    }

    val mgrInfo by lazy {
        MgrInfoModule()
    }

    val iccn by lazy {
        ICCNModule()
    }

    val news by lazy {
        MgrNewsModule()
    }

    val packageDownloader by lazy { OfficialCDNPackageDownloader(context) }
    val modDownloader by lazy { ModDownloader(context) }
    val mcLevelManager by lazy { MCLevelManager() }
    val levelTransporter by lazy {
        LevelTransporter(
            Environment.getExternalStorageDirectory().resolve("games").resolve("com.mojang")
                .resolve("minecraftWorlds").absolutePath
        )
    }
}
