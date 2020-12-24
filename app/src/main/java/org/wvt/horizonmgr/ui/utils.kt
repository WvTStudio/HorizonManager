package org.wvt.horizonmgr.ui

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticAmbientOf
import org.wvt.horizonmgr.HorizonManagerApplication
import org.wvt.horizonmgr.legacyservice.HorizonManager
import org.wvt.horizonmgr.legacyservice.LocalCache
import org.wvt.horizonmgr.legacyservice.WebAPI
import org.wvt.horizonmgr.utils.CoroutineDownloader

@Deprecated("Deprecated")
val AmbientHorizonManager = staticAmbientOf<HorizonManager>()

@Deprecated("Deprecated")
val AmbientCoroutineDownloader = staticAmbientOf<CoroutineDownloader>()

@Deprecated("Deprecated")
val AmbientWebAPI = staticAmbientOf<WebAPI>()

@Deprecated("Deprecated")
val AmbientLocalCache = staticAmbientOf<LocalCache>()

@Deprecated("Deprecated")
val AmbientNavigator = staticAmbientOf<NavigatorViewModel>()

// 用 Ambient 解决依赖
@Composable
fun AndroidDependenciesProvider(children: @Composable () -> Unit) {
    val api = remember { HorizonManagerApplication.container }

    val horizonMgr = remember { api.horizonManager }
    val webApiInstance = remember { api.webapi }
    val localCache = remember { api.localCache }

    Providers(
        AmbientHorizonManager provides horizonMgr,
        AmbientWebAPI provides webApiInstance,
        AmbientLocalCache provides localCache,
        content = children
    )
}

inline fun <reified T> Context.startActivity() {
    startActivity(Intent(this, T::class.java))
}