package org.wvt.horizonmgr.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.service.LocalCache
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.ui.*
import org.wvt.horizonmgr.ui.community.CommunityActivity
import org.wvt.horizonmgr.ui.donate.DonateActivity
import org.wvt.horizonmgr.ui.joingroup.JoinGroupActivity
import org.wvt.horizonmgr.ui.login.LoginActivity
import org.wvt.horizonmgr.ui.settings.SettingsActivity
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_HorizonManagerCompose_NoActionBar) // cancel the slash theme
        setContent { // Platform-Specified content
            var userInfo by remember { mutableStateOf<WebAPI.UserInfo?>(null) }
            var selectedPackage by remember { mutableStateOf<String?>(null) }
            val context = ContextAmbient.current
            val scope = rememberCoroutineScope()

            var showPermissionDialog by remember { mutableStateOf(false) }

            AndroidDependenciesProvider {
                val mgrInstance = HorizonManagerAmbient.current

                launchInComposition {
                    if (!hasPermission()) {
                        showPermissionDialog = true
                    }

                    userInfo = getUserInfo()
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
                            userInfo = userInfo,
                            requestLogin = {
                                scope.launch(Dispatchers.Main) {
                                    userInfo = try {
                                        startLoginActivity()
                                    } catch (e: Throwable) {
                                        // TODO 添加提示信息
                                        return@launch
                                    }
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
                                scope.launch(Dispatchers.Main) {
                                    try {
                                        openGame()
                                    } catch (e: Throwable) {
                                        // TODO 添加提示信息
                                    }
                                }
                            },
                            community = {
                                scope.launch(Dispatchers.Main) { context.startActivity<CommunityActivity>() }
                            },
                            joinGroup = {
                                scope.launch(Dispatchers.Main) { context.startActivity<JoinGroupActivity>() }
                            },
                            donate = {
                                scope.launch(Dispatchers.Main) { context.startActivity<DonateActivity>() }
                            },
                            settings = {
                                scope.launch(Dispatchers.Main) { context.startActivity<SettingsActivity>() }
                            }
                        )
                    }
                }
            }
        }
    }

    private suspend fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && Build.VERSION.PREVIEW_SDK_INT != 0) {
            @SuppressLint("NewApi")
            if (Environment.isExternalStorageManager()) return true else return false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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

    private suspend fun getSelectedPackageUUID(): String? = withContext(Dispatchers.IO) {
        getSharedPreferences("selected_package", Context.MODE_PRIVATE).getString("uuid", null)
    }

    private suspend fun saveSelectedPackageUUID(uuid: String?) = withContext(Dispatchers.IO) {
        getSharedPreferences("selected_package", Context.MODE_PRIVATE).edit {
            if (uuid == null) remove("uuid")
            else putString("uuid", uuid)
        }
    }

    private suspend fun openGame(): Boolean = withContext(Dispatchers.Main) {
        val horizonIntent =
            Intent(packageManager.getLaunchIntentForPackage("com.zheka.horizon"))
        try {
            startActivity(horizonIntent)
            return@withContext true
        } catch (e: Exception) {
        }
        val innerCoreIntent =
            Intent(packageManager.getLaunchIntentForPackage("com.zheka.horizon"))
        try {
            startActivity(innerCoreIntent)
            return@withContext true
        } catch (e: Exception) {
        }
        return@withContext false
    }

    private suspend fun startLoginActivity(): WebAPI.UserInfo? {
        return suspendCoroutine<WebAPI.UserInfo?> { cont ->
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == LoginActivity.LOGIN_SUCCESS) {
                    cont.resume(getUserInfo())
                } else {
                    cont.resume(null)
                }
            }.launch(Intent(this, LoginActivity::class.java))
        }
    }

    private suspend fun logout() {
        clearUserInfo()
    }
}

