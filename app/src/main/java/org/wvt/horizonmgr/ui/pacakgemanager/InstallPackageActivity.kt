package org.wvt.horizonmgr.ui.pacakgemanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.setContent
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.ui.AndroidDependenciesProvider
import org.wvt.horizonmgr.ui.HorizonManagerAmbient
import org.wvt.horizonmgr.ui.WebAPIAmbient
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class InstallPackageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidDependenciesProvider {
                AndroidHorizonManagerTheme {
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

                    val webApi = WebAPIAmbient.current
                    val horizonMgr = HorizonManagerAmbient.current

                    onActive {
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