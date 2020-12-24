package org.wvt.horizonmgr

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.wvt.horizonmgr.legacyservice.HorizonManager
import org.wvt.horizonmgr.legacyservice.LocalCache
import org.wvt.horizonmgr.legacyservice.WebAPI

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
}
