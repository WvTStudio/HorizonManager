package org.wvt.horizonmgr.ui

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticAmbientOf
import org.wvt.horizonmgr.HorizonManagerApplication
import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.service.LocalCache
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.utils.CoroutineDownloader

@Deprecated("Deprecated")
val HorizonManagerAmbient = staticAmbientOf<HorizonManager>()

@Deprecated("Deprecated")
val CoroutineDownloaderAmbient = staticAmbientOf<CoroutineDownloader>()

@Deprecated("Deprecated")
val WebAPIAmbient = staticAmbientOf<WebAPI>()

@Deprecated("Deprecated")
val LocalCacheAmbient = staticAmbientOf<LocalCache>()

@Deprecated("Deprecated")
val NavigatorAmbient = staticAmbientOf<NavigatorViewModel>()

// 用 Ambient 解决依赖前没有发现问题
@Composable
fun AndroidDependenciesProvider(children: @Composable () -> Unit) {
    val api = remember { HorizonManagerApplication.container }

    val horizonMgr = remember { api.horizonManager }
    val webApiInstance = remember { api.webapi }
    val localCache = remember { api.localCache }

    Providers(
        HorizonManagerAmbient provides horizonMgr,
        WebAPIAmbient provides webApiInstance,
        LocalCacheAmbient provides localCache,
        children = children
    )
}

inline fun <reified T> Context.startActivity() {
    startActivity(Intent(this, T::class.java))
}