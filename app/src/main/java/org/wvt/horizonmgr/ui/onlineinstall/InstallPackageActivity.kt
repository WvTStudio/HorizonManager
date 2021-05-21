package org.wvt.horizonmgr.ui.onlineinstall

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor

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

@AndroidEntryPoint
class InstallPackageActivity : AppCompatActivity() {
    companion object {
        const val CANCEL = 0
        const val SUCCEED = 1
    }

    private val viewModel by viewModels<InstallPackageViewModel>() /*{ defaultViewModelFactory }*/

    enum class Screen {
        CHOOSE_PACKAGE, EDIT_NAME, INSTALL
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidHorizonManagerTheme {

                val packages by viewModel.packages.collectAsState()
                val state by viewModel.state.collectAsState()

                var prevScreen by remember { mutableStateOf(Screen.CHOOSE_PACKAGE) }
                var screen by remember { mutableStateOf(Screen.CHOOSE_PACKAGE) }
                var chosenIndex by remember { mutableStateOf<Int>(-1) }

                DisposableEffect(Unit) {
                    viewModel.getPackages()
                    onDispose { }
                }

                BackHandler {
                    when (screen) {
                        Screen.CHOOSE_PACKAGE -> finish()
                        Screen.EDIT_NAME -> {
                            prevScreen = screen
                            screen = Screen.CHOOSE_PACKAGE
                        }
                        Screen.INSTALL -> {
                        }
                    }
                }

                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Column {
                        TopAppBar(
                            navigationIcon = {
                                if (screen == Screen.CHOOSE_PACKAGE || screen == Screen.EDIT_NAME) {
                                    IconButton(onClick = {
                                        when (screen) {
                                            Screen.CHOOSE_PACKAGE -> finish()
                                            Screen.EDIT_NAME -> {
                                                prevScreen = screen
                                                screen = Screen.CHOOSE_PACKAGE
                                            }
                                            else -> {
                                            }
                                        }
                                    }) {
                                        Icon(
                                            Icons.Filled.ArrowBack,
                                            contentDescription = "返回"
                                        )
                                    }
                                }
                            },
                            title = {
                                Crossfade(targetState = screen) { screen ->
                                    when (screen) {
                                        Screen.CHOOSE_PACKAGE -> Text("在线安装分包")
                                        Screen.EDIT_NAME -> Text("输入分包的名字")
                                        Screen.INSTALL -> Text(if (viewModel.totalProgress.collectAsState().value >= 1f) "安装完成" else "正在安装")
                                    }
                                }

                            },
                            backgroundColor = AppBarBackgroundColor
                        )
                        Box {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = screen == Screen.CHOOSE_PACKAGE,
                                enter = fadeIn() + slideInHorizontally(),
                                exit = fadeOut() + slideOutHorizontally()
                            ) {
                                Crossfade(state) { state ->
                                    when (state) {
                                        InstallPackageViewModel.State.Loading -> Box(
                                            Modifier.fillMaxSize(),
                                            Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                        InstallPackageViewModel.State.Succeed -> ChoosePackage(
                                            modifier = Modifier.fillMaxSize(),
                                            items = packages,
                                            onChoose = {
                                                chosenIndex = it
                                                viewModel.selectPackage(packages[it].uuid)
                                                prevScreen = screen
                                                screen = Screen.EDIT_NAME
                                            }
                                        )
                                        is InstallPackageViewModel.State.Error -> ErrorPage(
                                            modifier = Modifier.fillMaxSize(),
                                            message = { Text(state.message) },
                                            onRetryClick = { viewModel.getPackages() }
                                        )
                                    }
                                }
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = screen == Screen.EDIT_NAME,
                                enter = fadeIn() + slideInHorizontally({
                                    when (prevScreen) {
                                        Screen.CHOOSE_PACKAGE -> it / 2 // Slide in from right
                                        Screen.INSTALL -> -it / 2 // Slide in from left
                                        else -> it
                                    }
                                }),
                                exit = fadeOut() + slideOutHorizontally({
                                    when (screen) {
                                        Screen.CHOOSE_PACKAGE -> it / 2 // Slide out to right
                                        Screen.INSTALL -> -it / 2 // Slide out to left
                                        else -> it
                                    }
                                })
                            ) {
                                EditName(
                                    packages[chosenIndex].name,
                                    packages[chosenIndex].version,
                                    onConfirm = {
                                        viewModel.setCustomName(it)
                                        viewModel.startInstall()
                                        prevScreen = screen
                                        screen = Screen.INSTALL
                                    }
                                )
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = screen == Screen.INSTALL,
                                enter = fadeIn() + slideInHorizontally({
                                    it / 2  // Slide in from right
                                }),
                                exit = fadeOut() + slideOutHorizontally({
                                    it / 2 // Slide out to right
                                })
                            ) {
                                InstallProgress(
                                    totalProgress = viewModel.totalProgress.collectAsState().value,
                                    downloadState = viewModel.downloadState.collectAsState().value,
                                    installState = viewModel.installState.collectAsState().value,
                                    onCancelClick = { viewModel.cancelInstall() },
                                    onCompleteClick = { finishWithSucceed() }
                                )
                            }
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