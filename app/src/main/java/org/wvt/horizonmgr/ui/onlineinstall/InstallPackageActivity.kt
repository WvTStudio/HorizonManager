package org.wvt.horizonmgr.ui.onlineinstall

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class InstallPackageResultContract : ActivityResultContract<Context, Boolean>() {
    override fun createIntent(context: Context, input: Context?): Intent {
        return Intent(context, InstallPackageActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return when (resultCode) {
            InstallPackageActivity.SUCCEED -> true
            InstallPackageActivity.CANCEL -> false
            else -> false
        }
    }
}

class InstallPackageActivity : AppCompatActivity() {
    companion object {
        const val CANCEL = 0
        const val SUCCEED = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidHorizonManagerTheme {
                // TODO 2021/1/18 重构这坨屎
                val vm = dependenciesViewModel<InstallPackageViewModel>()
                val packages by vm.packages.collectAsState()

                var screen: Int by remember { mutableStateOf(0) }
                var chosenIndex by remember { mutableStateOf<Int>(-1) }

                DisposableEffect(Unit) {
                    onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            when (screen) {
                                0 -> finish()
                                1 -> screen = 0
                            }
                        }
                    })
                    vm.getPackages()
                    onDispose { }
                }

                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Crossfade(targetState = screen) { it ->
                        when (it) {
                            0 -> ChoosePackage(
                                onChoose = {
                                    chosenIndex = it
                                    vm.selectPackage(packages[it].uuid)
                                    screen = 1
                                },
                                onCancel = { finish() },
                                items = packages
                            )
                            1 -> EditName(
                                packages[chosenIndex].name,
                                packages[chosenIndex].version,
                                onConfirm = {
                                    vm.setCustomName(it)
                                    vm.startInstall()
                                    screen = 2
                                },
                                onCancel = { screen = 0 }
                            )
                            2 -> InstallProgress(
                                totalProgress = vm.totalProgress.collectAsState().value,
                                downloadState = vm.downloadState.collectAsState().value,
                                installState = vm.installState.collectAsState().value,
                                onCancelClick = { vm.cancelInstall() },
                                onCompleteClick = { finishWithSucceed() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun finishWithSucceed() {
        setResult(SUCCEED)
        finish()
    }
}