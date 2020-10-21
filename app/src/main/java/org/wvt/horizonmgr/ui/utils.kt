package org.wvt.horizonmgr.ui

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticAmbientOf
import androidx.compose.ui.platform.ContextAmbient
import androidx.core.content.edit
import org.wvt.horizonmgr.HorizonManagerApplication
import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.service.LocalCache
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.utils.CoroutineDownloader

val HorizonManagerAmbient = staticAmbientOf<HorizonManager>()
val CoroutineDownloaderAmbient = staticAmbientOf<CoroutineDownloader>()
val WebAPIAmbient = staticAmbientOf<WebAPI>()
val LocalCacheAmbient = staticAmbientOf<LocalCache>()
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