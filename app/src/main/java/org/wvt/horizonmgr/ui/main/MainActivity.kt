package org.wvt.horizonmgr.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.HorizonManagerApplication
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.service.LocalCache
import org.wvt.horizonmgr.ui.AndroidDependenciesProvider
import org.wvt.horizonmgr.ui.HorizonManagerAmbient
import org.wvt.horizonmgr.ui.community.CommunityActivity
import org.wvt.horizonmgr.ui.donate.DonateActivity
import org.wvt.horizonmgr.ui.joingroup.JoinGroupActivity
import org.wvt.horizonmgr.ui.login.LoginActivity
import org.wvt.horizonmgr.ui.settings.SettingsActivity
import org.wvt.horizonmgr.ui.startActivity
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import kotlin.coroutines.resume

class MainActivity : AppCompatActivity() {
    private val dependencies = HorizonManagerApplication.container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_HorizonManagerCompose_NoActionBar) // cancel the slash theme
        setContent { // Platform-Specified content
            var userInfo by remember { mutableStateOf<LocalCache.CachedUserInfo?>(null) }
            var selectedPackage by remember { mutableStateOf<String?>(null) }
            val context = ContextAmbient.current
            val scope = rememberCoroutineScope()

            var showPermissionDialog by remember { mutableStateOf(false) }

            AndroidDependenciesProvider {
                val mgrInstance = HorizonManagerAmbient.current

                LaunchedTask {
                    if (!hasPermission()) {
                        showPermissionDialog = true
                    }
                    userInfo = dependencies.localCache.getCachedUserInfo()
                    val s = getSelectedPackageUUID()
                    selectedPackage =
                        if (s != null && mgrInstance.getPackageInfo(s) != null) s
                        else null
                }

                AndroidHorizonManagerTheme {
                    if (showPermissionDialog) {
                        AlertDialog(
                            onDismissRequest = { showPermissionDialog = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    showPermissionDialog = false
                                    scope.launch(Dispatchers.Main) {
                                        requestPermission()
                                    }
                                }) { Text(text = "确定") }
                            },
                            title = { Text("需要权限") },
                            text = {
                                Text("本应用需要拥有网络权限及对内置存储的完全访问权限。\n如果您的系统版本为 Android R 及以上，您需要在弹出的系统设置中授予完全访问权限")
                            })
                    }

                    Surface(color = MaterialTheme.colors.background) {
                        App(
                            dependencies = dependencies,
                            userInfo = userInfo,
                            requestLogin = {
                                scope.launch {
                                    val r = try {
                                        startLoginActivity()
                                    } catch (e: Throwable) {
                                        // TODO 添加提示信息
                                        return@launch
                                    }
                                    r?.let {
                                        dependencies.localCache.cacheUserInfo(
                                            it.id, it.name, it.account, it.avatarUrl
                                        )
                                    }
                                    userInfo = r
                                }
                            },
                            requestLogout = {
                                scope.launch {
                                    try {
                                        logout()
                                    } catch (e: Throwable) {
                                        // TODO 添加提示信息
                                    }
                                    userInfo = null
                                }
                            },
                            selectedPackageUUID = selectedPackage,
                            selectedPackageChange = {
                                scope.launch {
                                    try {
                                        saveSelectedPackageUUID(it)
                                        selectedPackage = it
                                    } catch (e: Throwable) {
                                    }
                                }
                            },
                            openGame = {
                                scope.launch {
                                    try {
                                        openGame()
                                    } catch (e: Throwable) {
                                        // TODO 添加提示信息
                                    }
                                }
                            },
                            community = {
                                scope.launch { context.startActivity<CommunityActivity>() }
                            },
                            joinGroup = {
                                scope.launch { context.startActivity<JoinGroupActivity>() }
                            },
                            donate = {
                                scope.launch { context.startActivity<DonateActivity>() }
                            },
                            settings = {
                                scope.launch { context.startActivity<SettingsActivity>() }
                            },
                        )
                    }
                }
            }
        }
    }

    private suspend fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            listOf<String>(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET
            ).forEach {
                if (checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) return false
            }
        }
        return true
    }

    private suspend fun requestPermission(): Boolean {
        // TODO: 2020/10/13 支持挂起，在用户完成操作后恢复
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && Build.VERSION.PREVIEW_SDK_INT != 0) {
            // R Preview
            @SuppressLint("NewApi")
            if (!Environment.isExternalStorageManager()) startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET
                ), 0
            )
        }
        return true
    }

    private suspend fun getSelectedPackageUUID(): String? =
        dependencies.localCache.getSelectedPackageUUID()

    private suspend fun saveSelectedPackageUUID(uuid: String?) =
        dependencies.localCache.setSelectedPackageUUID(uuid)

    private suspend fun openGame(): Boolean = withContext(Dispatchers.Main) {
        try {
            val horizonIntent =
                Intent(packageManager.getLaunchIntentForPackage("com.zheka.horizon"))
            startActivity(horizonIntent)
            return@withContext true
        } catch (e: Exception) {
        }

        try {
            val innerCoreIntent =
                Intent(packageManager.getLaunchIntentForPackage("com.zheka.horizon"))
            startActivity(innerCoreIntent)
            return@withContext true
        } catch (e: Exception) {
        }

        return@withContext false
    }

    private suspend fun startLoginActivity(): LocalCache.CachedUserInfo? {
        return suspendCancellableCoroutine { cont ->
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == LoginActivity.LOGIN_SUCCESS) {
                    val data = it.data ?: return@registerForActivityResult cont.resume(null)
                    val result = with(LoginActivity) { data.getResult() }
                    cont.resume(result)
                } else {
                    cont.resume(null)
                }
            }.launch(Intent(this, LoginActivity::class.java))
        }
    }


    private suspend fun logout() {
        dependencies.localCache.clearCachedUserInfo()
    }
}

