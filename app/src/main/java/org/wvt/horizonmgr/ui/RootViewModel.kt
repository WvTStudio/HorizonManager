package org.wvt.horizonmgr.ui

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.BuildConfig
import org.wvt.horizonmgr.utils.LocalCache
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.mgrinfo.MgrInfoModule
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RootViewModel"

@Singleton
class RootViewModel @Inject constructor(
    private val localCache: LocalCache,
    private val mgrInfo: MgrInfoModule
) {
    val viewModelScope = CoroutineScope(Dispatchers.Default)
    val showPermissionDialog: MutableStateFlow<Boolean> = MutableStateFlow(false)

    data class NewVersion(
        val versionName: String,
        val versionCode: Int,
        val changelog: String
    )

    val newVersion: MutableStateFlow<NewVersion?> = MutableStateFlow(null)

    private var ignoreVersion: Int? = null

    val gameNotInstalled = MutableStateFlow(false)

    val hzNotInstalled = MutableStateFlow(false)

    fun checkUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(
                TAG,
                "Build Type: ${BuildConfig.BUILD_TYPE}, Version Code: ${BuildConfig.VERSION_CODE}"
            )

            // 获取本地忽略的最新版本号
            ignoreVersion = localCache.getIgnoreVersion()
            val ig = ignoreVersion

            val channel = try {
                mgrInfo.getChannelByName(BuildConfig.BUILD_TYPE)
            } catch (e: NetworkException) {
                // TODO: 2021/2/8 添加网络错误的逻辑
                return@launch
            } catch (e: Exception) {
                Log.e(TAG, "获取版本通道失败", e)
                return@launch
            } ?: return@launch

            // 查找当前 Channel 的最新版本
            val latest = try {
                channel.latestVersion()
            } catch (e: NetworkException) {
                // TODO: 2021/2/8 添加网络错误的逻辑
                return@launch
            } catch (e: Exception) {
                Log.e(TAG, "获取最新版本信息失败", e)
                return@launch
            }

            Log.d(TAG, "latestVersion: $latest")

            // 如果最新版本不比当前版本大则退出
            if (latest.versionCode <= BuildConfig.VERSION_CODE) return@launch

            // 如果最新版本不比忽略的版本大，则退出
            if (ig != null && latest.versionCode <= ig) return@launch

            // 获取最新版本的信息
            val versionData = try {
                latest.getData()
            } catch (e: NetworkException) {
                // TODO: 2021/2/8 添加网络错误的逻辑
                return@launch
            } catch (e: Exception) {
                Log.e(TAG, "获取版本数据失败", e)
                return@launch
            }

            newVersion.value = NewVersion(
                versionName = versionData.versionName,
                versionCode = versionData.versionCode,
                changelog = versionData.changeLog
            )
        }
    }

    fun dismiss() {
        showPermissionDialog.value = false
    }

    fun showPermissionDialog() {
        viewModelScope.launch {
            showPermissionDialog.value = true
        }
    }

    fun ignoreVersion(versionCode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            localCache.setIgnoreVersion(versionCode)
        }
    }

    fun showGameNotInstallDialog() {
        viewModelScope.launch {
            if (!localCache.isNeverShowMCInstallationTip())
                gameNotInstalled.emit(true)
        }
    }

    fun dismissGameNotInstallDialog() {
        viewModelScope.launch {
            gameNotInstalled.emit(false)
        }
    }

    fun showHZNotInstallDialog() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!localCache.isNeverShowHZInstallationTip())
                hzNotInstalled.emit(true)
        }
    }

    fun dismissHZNotInstallDialog() {
        viewModelScope.launch {
            hzNotInstalled.emit(false)
        }
    }

    fun neverShowHZInstallationTip() {
        viewModelScope.launch {
            hzNotInstalled.emit(false)
            localCache.setNeverShowHZInstallationTip(true)
        }
    }

    fun neverShowMCInstallationTip() {
        viewModelScope.launch {
            gameNotInstalled.emit(false)
            localCache.setNeverShowMCInstallationTip(true)
        }
    }
}