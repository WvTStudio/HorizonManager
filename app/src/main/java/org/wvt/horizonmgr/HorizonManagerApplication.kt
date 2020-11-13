package org.wvt.horizonmgr

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.service.LocalCache
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.service.webapi2.WebAPI2

class HorizonManagerApplication : Application() {
    companion object {
        lateinit var container: DependenciesContainer
            private set
        lateinit var dependenciesVMFactory: ViewModelProvider.Factory
            private set
    }

    override fun onCreate() {
        super.onCreate()
        container = DependenciesContainerImpl(this)
        dependenciesVMFactory = DependenciesVMFactory(container)
    }
}

@Composable
inline fun <reified T : ViewModel> dependenciesViewModel(): T {
    return viewModel(factory = HorizonManagerApplication.dependenciesVMFactory)
}

private class DependenciesVMFactory(
    private val dependenciesContainer: DependenciesContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(p0: Class<T>): T {
        return p0.getConstructor(DependenciesContainer::class.java)
            .newInstance(dependenciesContainer)
    }
}

private class DependenciesContainerImpl(private val context: Context) : DependenciesContainer {
    override val horizonManager: HorizonManager by lazy {
        HorizonManager.getOrCreateInstance(context)
    }

    override val localCache: LocalCache by lazy {
        LocalCache.createInstance(context)
    }

    override val webapi: WebAPI by lazy {
        WebAPI.createInstance(context)
    }

    override val webapi2: WebAPI2 get() = TODO("Not yet implemented")
}
