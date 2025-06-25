package org.wvt.horizonmgr.ui.onlineinstall

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.service.hzpack.recommendDescription
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.pacakgemanager.ManifestSection
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor
import org.wvt.horizonmgr.viewmodel.InstallPackageViewModel

private enum class Screen {
    CHOOSE_PACKAGE, EDIT_NAME, INSTALL
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnlineInstallScreen(
    viewModel: InstallPackageViewModel,
    onCancel: () -> Unit,
    onSucceed: () -> Unit
) {
    val packages by viewModel.packages.collectAsState()
    val state by viewModel.getPackageState.collectAsState()

    var screen by remember { mutableStateOf(Screen.CHOOSE_PACKAGE) }
    var chosenIndex by remember { mutableStateOf<Int>(-1) }

    DisposableEffect(Unit) {
        viewModel.getPackages()
        onDispose { }
    }

    BackHandler {
        when (screen) {
            Screen.CHOOSE_PACKAGE -> onCancel()
            Screen.EDIT_NAME -> {
                screen = Screen.CHOOSE_PACKAGE
            }
            Screen.INSTALL -> {
                viewModel.cancelInstall()
            }
        }
    }

    Column {
        TopAppBar(
            navigationIcon = {
                if (screen == Screen.CHOOSE_PACKAGE || screen == Screen.EDIT_NAME) {
                    IconButton(onClick = {
                        when (screen) {
                            Screen.CHOOSE_PACKAGE -> onCancel()
                            Screen.EDIT_NAME -> screen = Screen.CHOOSE_PACKAGE
                            else -> viewModel.cancelInstall()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.navigation_action_back)
                        )
                    }
                }
            },
            title = {
                Crossfade(targetState = screen) { screen ->
                    when (screen) {
                        Screen.CHOOSE_PACKAGE -> Text(stringResource(R.string.ol_install_screen_title_choose))
                        Screen.EDIT_NAME -> Text(stringResource(R.string.ol_install_screen_title_edit))
                        Screen.INSTALL -> Text(
                            if (viewModel.totalProgress.collectAsState().value >= 1f)
                                stringResource(R.string.ol_install_screen_title_install_completed)
                            else stringResource(R.string.ol_install_screen_title_install_doing)
                        )
                    }
                }

            },
            backgroundColor = AppBarBackgroundColor
        )
        Box(Modifier.weight(1f)) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter =
                        if (targetState > initialState) fadeIn() + slideIntoContainer(
                            SlideDirection.Start
                        )
                        else fadeIn() + slideIntoContainer(
                            SlideDirection.End
                        ),
                        initialContentExit =
                        if (targetState > initialState) fadeOut() + slideOutOfContainer(
                            SlideDirection.Start
                        )
                        else fadeOut() + slideOutOfContainer(
                            SlideDirection.End
                        )
                    )
                }
            ) {
                when (it) {
                    Screen.CHOOSE_PACKAGE ->
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
                    Screen.EDIT_NAME -> Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        EditName(
                            Modifier
                                .wrapContentHeight()
                                .fillMaxWidth(),
                            packages[chosenIndex].name,
                            packages[chosenIndex].version,
                            onConfirm = {
                                viewModel.setCustomName(it)
                                viewModel.startInstall()
                                screen = Screen.INSTALL
                            }
                        )
                        val manifest by viewModel.selectedPackageManifest.collectAsState()
                        AnimatedContent(
                            targetState = manifest,
                            transitionSpec = { ContentTransform(fadeIn(), fadeOut()) }
                        ) { state ->
                            if (state == null) {
                                Box(
                                    Modifier
                                        .padding(32.dp)
                                        .fillMaxWidth()
                                ) {
                                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                                }
                            } else with(state) {
                                ManifestSection(
                                    modifier = Modifier.wrapContentHeight(),
                                    packageName = pack,
                                    developer = developer,
                                    versionName = packVersion,
                                    versionCode = packVersionCode.toString(),
                                    packageUUID = pack,
                                    gameVersion = gameVersion,
                                    description = recommendDescription()
                                )
                            }
                        }
                    }
                    Screen.INSTALL -> InstallProgress(
                        totalProgress = viewModel.totalProgress.collectAsState().value,
                        installState = viewModel.installState.collectAsState().value,
                        mergeState = viewModel.mergeState.collectAsState().value,
                        downloadSteps = viewModel.downloadSteps.collectAsState().value,
                        onCancelClick = { viewModel.cancelInstall() },
                        onCloseClick = { onCancel() },
                        onCompleteClick = onSucceed
                    )
                }
            }
        }
    }
}