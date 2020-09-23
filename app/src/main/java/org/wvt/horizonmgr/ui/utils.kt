package org.wvt.horizonmgr.ui

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticAmbientOf
import androidx.compose.ui.platform.ContextAmbient
import androidx.core.content.edit
import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.service.LocalCache
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.utils.CoroutineDownloader

val HorizonManagerAmbient = staticAmbientOf<HorizonManager>()
val CoroutineDownloaderAmbient = staticAmbientOf<CoroutineDownloader>()
val WebAPIAmbient = staticAmbientOf<WebAPI>()
val LocalCacheAmbient = staticAmbientOf<LocalCache>()

// 用 Ambient 解决依赖前没有发现问题
@Composable
fun AndroidDependenciesProvider(children: @Composable () -> Unit) {
    val context = ContextAmbient.current
    val horizonMgr = remember { HorizonManager.getOrCreateInstance(context) }
    val webApiInstance = remember { WebAPI.getOrCreate(context) }
    val localCache = remember { LocalCache.createInstance(context) }

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

// TODO Move this to [LocalCache]
fun Context.saveUserInfo(userInfo: WebAPI.UserInfo) {
    getSharedPreferences("user_info", Context.MODE_PRIVATE).edit {
        putInt("id", userInfo.id)
        putString("account", userInfo.account)
        putString("avatar_url", userInfo.avatarUrl)
        putString("name", userInfo.name)
    }
}

// TODO Move this to [LocalCache]
fun Context.clearUserInfo() {
    getSharedPreferences("user_info", Context.MODE_PRIVATE).edit {
        clear()
    }
}

// TODO Move this to [LocalCache]
fun Context.getUserInfo() =
    with(getSharedPreferences("user_info", Context.MODE_PRIVATE)) {
        val name = getString("name", null) ?: return@with null
        val account = getString("account", null) ?: return@with null
        val avatarUrl = getString("avatar_url", null) ?: return@with null
        val id = getInt("id", -1)
        WebAPI.UserInfo(id, account, name, avatarUrl)
    }
