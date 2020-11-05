package org.wvt.horizonmgr.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.BuildConfig
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.LocalCache
import org.wvt.horizonmgr.ui.login.LoginActivity
import kotlin.coroutines.resume

class MainActivityViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {
    val userInfo: MutableStateFlow<LocalCache.CachedUserInfo?> = MutableStateFlow(null)
    val selectedPackage: MutableStateFlow<String?> = MutableStateFlow(null)
    val showPermissionDialog: MutableStateFlow<Boolean> = MutableStateFlow(false)

    data class NewVersion(
        val versionName: String,
        val versionCode: Int,
        val changelog: String
    )

    val newVersion: MutableStateFlow<NewVersion?> = MutableStateFlow(null)

    private var ignoreVersion: Int? = null

    init {
        viewModelScope.launch {
            userInfo.value = dependencies.localCache.getCachedUserInfo()
            selectedPackage.value = dependencies.localCache.getSelectedPackageUUID()
        }
        getUpdate()
    }

    fun getUpdate() {
        viewModelScope.launch {
            Log.d(
                "MainVM",
                "build_type: " + BuildConfig.BUILD_TYPE + ", version_code: " + BuildConfig.VERSION_CODE
            )

            // 获取本地忽略的最新版本号
            ignoreVersion = dependencies.localCache.getIgnoreVersion()
            val ig = ignoreVersion

            // 查找当前 Channel 的最新版本
            val l = try {
                dependencies.webapi.getLatestVersions().find {
                    it.channel == BuildConfig.BUILD_TYPE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@launch
            } ?: return@launch

            Log.d("MainVM", "latestversion: $l")

            // 如果最新版本不比当前版本大则退出
            if (l.latestVersionCode <= BuildConfig.VERSION_CODE) return@launch

            // 如果最新版本不比忽略的版本大，则退出
            if (ig != null && l.latestVersionCode <= ig) return@launch

            // 获取最新版本的信息
            val v = try {
                dependencies.webapi.getChangelogs().find {
                    it.versionCode == l.latestVersionCode
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@launch
            } ?: return@launch

            newVersion.value = NewVersion(
                versionName = v.versionName,
                versionCode = v.versionCode,
                changelog = v.changelog
            )
        }
    }

    fun setSelectedPackage(uuid: String?) {
        viewModelScope.launch {
            dependencies.localCache.setSelectedPackageUUID(uuid)
            selectedPackage.value = uuid
        }
    }

    fun setUserInfo(userInfo: LocalCache.CachedUserInfo?) {
        viewModelScope.launch {
            if (userInfo == null) dependencies.localCache.clearCachedUserInfo()
            else dependencies.localCache.cacheUserInfo(
                userInfo.id,
                userInfo.name,
                userInfo.account,
                userInfo.avatarUrl
            )
            this@MainActivityViewModel.userInfo.value = userInfo
        }
    }

    fun logOut() {
        viewModelScope.launch {
            dependencies.localCache.clearCachedUserInfo()
            userInfo.value = null
        }
    }

    fun checkPermission(context: Activity) {
        fun check(): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return Environment.isExternalStorageManager()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                listOf<String>(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET
                ).forEach {
                    if (context.checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) return false
                }
            }
            return true
        }
        viewModelScope.launch {
            showPermissionDialog.value = !check()
        }
    }

    fun dismiss() {
        showPermissionDialog.value = false
    }

    fun requestPermission(context: Activity) {
        // TODO: 2020/10/13 支持挂起，在用户完成操作后恢复
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && Build.VERSION.PREVIEW_SDK_INT != 0) {
            // R Preview
            @SuppressLint("NewApi")
            if (!Environment.isExternalStorageManager()) context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET
                ), 0
            )
        }
    }

    fun openGame(context: Activity) {
        try {
            val horizonIntent =
                Intent(context.packageManager.getLaunchIntentForPackage("com.zheka.horizon"))
            context.startActivity(horizonIntent)
            return
        } catch (e: Exception) {
        }

        try {
            val innerCoreIntent =
                Intent(context.packageManager.getLaunchIntentForPackage("com.zheka.horizon"))
            context.startActivity(innerCoreIntent)
            return
        } catch (e: Exception) {
        }
    }

    fun requestLogin(context: ComponentActivity) {
        viewModelScope.launch {
            val userInfo = withContext(Dispatchers.Main) { startLoginActivity(context) }
            setUserInfo(userInfo)
        }
    }

    fun ignoreVersion(versionCode: Int) {
        viewModelScope.launch {
            dependencies.localCache.setIgnoreVersion(versionCode)
        }
    }

    private suspend fun startLoginActivity(activity: ComponentActivity): LocalCache.CachedUserInfo? {
        return suspendCancellableCoroutine { cont ->
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == LoginActivity.LOGIN_SUCCESS) {
                    val data = it.data ?: return@registerForActivityResult cont.resume(null)
                    val result = with(LoginActivity) { data.getResult() }
                    cont.resume(result)
                } else {
                    cont.resume(null)
                }
            }.launch(Intent(activity, LoginActivity::class.java))
        }
    }
}