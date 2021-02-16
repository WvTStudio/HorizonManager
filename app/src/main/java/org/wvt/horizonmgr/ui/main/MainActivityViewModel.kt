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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.wvt.horizonmgr.BuildConfig
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.legacyservice.LocalCache
import org.wvt.horizonmgr.ui.login.LoginActivity
import org.wvt.horizonmgr.ui.login.LoginResult
import org.wvt.horizonmgr.ui.login.startForResult
import org.wvt.horizonmgr.webapi.NetworkException
import kotlin.coroutines.resume

private const val TAG = "MainActivityVM"

class MainActivityViewModel(
    dependencies: DependenciesContainer
) : ViewModel() {
    private val localCache = dependencies.localCache
    private val mgrInfo = dependencies.mgrInfo

    var initializing = MutableStateFlow(true)

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
            userInfo.value = localCache.getCachedUserInfo()
            selectedPackage.value = localCache.getSelectedPackageUUID()
            initializing.value = false
        }
    }

    fun getUpdate() {
        viewModelScope.launch {
            Log.d(
                TAG,
                "build_type: " + BuildConfig.BUILD_TYPE + ", " +
                        "version_code: " + BuildConfig.VERSION_CODE
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

    fun setSelectedPackage(uuid: String?) {
        viewModelScope.launch {
            localCache.setSelectedPackageUUID(uuid)
            selectedPackage.value = uuid
        }
    }

    fun setUserInfo(userInfo: LoginResult) {
        viewModelScope.launch {
            if (userInfo is LoginResult.Succeed) {
                localCache.cacheUserInfo(
                    userInfo.uid,
                    userInfo.name,
                    userInfo.account,
                    userInfo.avatar
                )
            }
            this@MainActivityViewModel.userInfo.value = localCache.getCachedUserInfo()
            /*
            if (userInfo == null) localCache.clearCachedUserInfo()
            else localCache.cacheUserInfo(
                userInfo.uid,
                userInfo.name,
                userInfo.account,
                userInfo.avatarUrl
            )
             = userInfo*/
        }
    }

    fun logOut() {
        viewModelScope.launch {
            localCache.clearCachedUserInfo()
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

        // TODO: 2021/2/8 全都打开失败后提示
    }

    fun requestLogin(context: AppCompatActivity) {
        GlobalScope.launch {
            Log.d(TAG, "Start activity")
            val userInfo = LoginActivity.startForResult(context)
//            val userInfo = withContext(Dispatchers.Main) { startLoginActivity(context) }
            setUserInfo(userInfo)
            Log.d(TAG, "Activity resulted")
        }
    }

    fun ignoreVersion(versionCode: Int) {
        viewModelScope.launch {
            localCache.setIgnoreVersion(versionCode)
        }
    }

    private suspend fun startLoginActivity(activity: ComponentActivity): LocalCache.CachedUserInfo? {
        return suspendCancellableCoroutine { cont ->
            activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == LoginActivity.LOGIN_SUCCESS) {
                    val data = it.data ?: return@registerForActivityResult cont.resume(null)
                    val result = with(LoginActivity) { data.getResult() }
                    Log.d(TAG, "Login result: ${result.toString()}")
                    cont.resume(result)
                } else {
                    cont.resume(null)
                }
            }.launch(Intent(activity, LoginActivity::class.java))
        }
    }
}