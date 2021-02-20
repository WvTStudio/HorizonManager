package org.wvt.horizonmgr

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.wvt.horizonmgr.legacyservice.HorizonManager
import org.wvt.horizonmgr.legacyservice.LocalCache
import org.wvt.horizonmgr.legacyservice.OfficialCDNPackageDownloader
import org.wvt.horizonmgr.legacyservice.WebAPI
import org.wvt.horizonmgr.webapi.iccn.ICCNModule
import org.wvt.horizonmgr.webapi.mgrinfo.MgrInfoModule
import org.wvt.horizonmgr.webapi.mod.ChineseModRepository
import org.wvt.horizonmgr.webapi.mod.OfficialModCDNRepository
import org.wvt.horizonmgr.webapi.news.MgrNewsModule
import org.wvt.horizonmgr.webapi.pack.OfficialPackageCDNRepository

class HorizonManagerApplication : Application() {
    lateinit var container: DependenciesContainer
        private set
    lateinit var dependenciesVMFactory: ViewModelProvider.Factory
        private set

    override fun onCreate() {
        super.onCreate()
        container = DependenciesContainer(this)
        dependenciesVMFactory = DependenciesVMFactory(container)
    }
}

@Composable
inline fun <reified T : ViewModel> dependenciesViewModel(): T {
    val app = LocalContext.current.applicationContext as HorizonManagerApplication
    return viewModel(factory = app.dependenciesVMFactory)
}

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

    @Deprecated("Deprecated")
    val webapi: WebAPI by lazy {
        WebAPI.createInstance(context)
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

    val cdnModRepository by lazy {
        OfficialModCDNRepository()
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

    val downloader by lazy { OfficialCDNPackageDownloader(context) }
}
