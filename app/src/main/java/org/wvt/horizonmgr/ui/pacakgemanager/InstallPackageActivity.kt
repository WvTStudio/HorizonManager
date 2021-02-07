package org.wvt.horizonmgr.ui.pacakgemanager

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.setContent
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.wvt.horizonmgr.legacyservice.WebAPI
import org.wvt.horizonmgr.ui.AmbientHorizonManager
import org.wvt.horizonmgr.ui.AmbientWebAPI
import org.wvt.horizonmgr.ui.AndroidDependenciesProvider
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import kotlin.coroutines.resume

class InstallPackageActivity : AppCompatActivity() {
    companion object {
        const val CANCEL = 0
        const val SUCCEED = 1

        suspend fun startForResult(context: ComponentActivity): Boolean {
            return suspendCancellableCoroutine<Boolean> { cont ->
                context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    when (it.resultCode) {
                        SUCCEED -> {
                            if (cont.isActive) cont.resume(true)
                        }
                        CANCEL -> {
                            if (cont.isActive) cont.resume(false)
                        }
                    }
                }.launch(Intent(context, InstallPackageActivity::class.java))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidDependenciesProvider {
                AndroidHorizonManagerTheme {
                    // TODO 2021/1/18 重构这坨屎
                    var screen: Int by remember { mutableStateOf(0) }
                    var packages by remember { mutableStateOf<List<WebAPI.ICPackage>>(emptyList()) }
                    var mappedPackages by remember {
                        mutableStateOf<List<ChoosePackageItem>>(
                            emptyList()
                        )
                    }
                    var chosenIndex by remember { mutableStateOf<Int>(-1) }
                    var customName by remember { mutableStateOf<String?>(null) }
                    val scope = rememberCoroutineScope()

                    val webApi = AmbientWebAPI.current
                    val horizonMgr = AmbientHorizonManager.current

                    DisposableEffect(Unit) {
                        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                            override fun handleOnBackPressed() {
                                when (screen) {
                                    0 -> finish()
                                    1 -> screen = 0
                                }
                            }
                        })
                        scope.launch {
                            packages = try {
                                webApi.getPackages()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                // TODO 显示错误提示和重试选项
                                return@launch
                            }
                            mappedPackages = packages.map {
                                val manifest = horizonMgr.parsePackageManifest(it.manifestStr)
                                ChoosePackageItem(
                                    manifest.packName,
                                    manifest.packVersionName,
                                    it.recommended
                                )
                            }
                        }
                        onDispose {
                            // TODO: 2021/2/7 添加 Dispose 逻辑
                        }
                    }

                    Surface {
                        Crossfade(current = screen) { it ->
                            when (it) {
                                0 -> ChoosePackage(
                                    onChoose = {
                                        chosenIndex = it
                                        screen = 1
                                    },
                                    onCancel = { finish() },
                                    items = mappedPackages
                                )
                                1 -> EditName(mappedPackages[chosenIndex],
                                    onConfirm = {
                                        customName = it
                                        screen = 2
                                    },
                                    onCancel = { screen = 0 }
                                )
                                2 -> InstallPackage(packages[chosenIndex], customName!!) {
                                    finish()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}