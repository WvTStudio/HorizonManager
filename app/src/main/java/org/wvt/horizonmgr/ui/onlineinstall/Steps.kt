package org.wvt.horizonmgr.ui.onlineinstall

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.theme.PreviewTheme
import org.wvt.horizonmgr.utils.longSizeToString
import org.wvt.horizonmgr.viewmodel.InstallPackageViewModel.DownloadStep
import org.wvt.horizonmgr.viewmodel.InstallPackageViewModel.StepState

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun StepItem(
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
    contentColor: Color = LocalContentColor.current
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        ListItem(
            modifier = Modifier.height(72.dp),
            icon = icon, text = text, trailing = trailing
        )
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DownloadStep(data: DownloadStep) {
    val downloadState by data.state
    val contentColor = when (downloadState) {
        is DownloadStep.State.Waiting -> MaterialTheme.colors.onBackground.copy(ContentAlpha.disabled)
        is DownloadStep.State.Error -> MaterialTheme.colors.error
        is DownloadStep.State.Complete -> Color(0xFF33691E)
        else -> MaterialTheme.colors.onBackground
    }
    StepItem(
        contentColor = contentColor,
        icon = {
            Icon(Icons.Rounded.CloudDownload, null)
        },
        text = {
            when (val downloadState = downloadState) {
                DownloadStep.State.Waiting -> Text(text = stringResource(R.string.ol_install_screen_step_download_waiting))
                DownloadStep.State.Complete -> Text(text = stringResource(R.string.ol_install_screen_step_download_completed))
                is DownloadStep.State.Running -> {
                    val downloaded = remember(downloadState.progress.value) {
                        longSizeToString(downloadState.progress.value)
                    }
                    val total = remember { longSizeToString(downloadState.total) }
                    Text(text = stringResource(R.string.ol_install_screen_step_download_running) + " ($downloaded / $total)")
                }
                is DownloadStep.State.Error -> Text(text = stringResource(R.string.ol_install_screen_step_download_failed))
            }

        }, trailing = {
            Crossfade(targetState = downloadState) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    when (it) {
                        DownloadStep.State.Waiting -> {
                        }
                        DownloadStep.State.Complete -> {
                            Icon(Icons.Rounded.Check, null)
                        }
                        is DownloadStep.State.Running -> {
                            val progress by rememberUpdatedState(it.progress.value.toFloat() / it.total.toFloat())
                            CircularProgressIndicator(progress = animateFloatAsState(progress).value)
                        }
                        is DownloadStep.State.Error -> {
                            var showError by remember { mutableStateOf(false) }
                            IconButton(onClick = { showError = true }) {
                                Icon(Icons.Rounded.Error, contentDescription = null)
                            }
                            if (showError) AlertDialog(
                                onDismissRequest = { showError = false },
                                title = { Text(stringResource(R.string.ol_install_screen_step_download_dialog_title)) },
                                text = { Text(it.message) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showError = false
                                    }) { Text(stringResource(R.string.button_action_confirm)) }
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun MergeStep(mergeState: StepState) {
    val contentColor = when (mergeState) {
        is StepState.Waiting -> MaterialTheme.colors.onBackground.copy(ContentAlpha.disabled)
        else -> MaterialTheme.colors.onBackground
    }
    StepItem(
        icon = {
            Icon(Icons.Rounded.Inbox, null)
        }, text = {
            Text(
                text = when (mergeState) {
                    StepState.Waiting -> stringResource(R.string.ol_install_screen_step_merge_waiting)
                    StepState.Complete -> stringResource(R.string.ol_install_screen_step_merge_completed)
                    is StepState.Running -> stringResource(R.string.ol_install_screen_step_merge_running)
                    is StepState.Error -> stringResource(R.string.ol_install_screen_step_merge_failed)
                },
                color = contentColor
            )
        }, trailing = {
            Crossfade(targetState = mergeState) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    when (it) {
                        StepState.Waiting -> {
                        }
                        StepState.Complete -> {
                            Icon(Icons.Rounded.Check, null)
                        }
                        is StepState.Running -> {
                            CircularProgressIndicator()
                        }
                        is StepState.Error -> {
                            Icon(Icons.Rounded.Error, null)
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InstallStep(installState: StepState) {
    val contentColor = when (installState) {
        is StepState.Waiting -> MaterialTheme.colors.onBackground.copy(ContentAlpha.disabled)
        else -> MaterialTheme.colors.onBackground
    }
    StepItem(
        icon = {
            Icon(Icons.Rounded.GetApp, null)
        }, text = {
            Text(
                text = when (installState) {
                    StepState.Waiting -> stringResource(R.string.ol_install_screen_step_install_waiting)
                    StepState.Complete -> stringResource(R.string.ol_install_screen_step_install_completed)
                    is StepState.Running -> stringResource(R.string.ol_install_screen_step_install_running)
                    is StepState.Error -> stringResource(R.string.ol_install_screen_step_install_failed)
                },
                color = contentColor
            )
        }, trailing = {
            AnimatedContent(targetState = installState) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    when (it) {
                        StepState.Waiting -> {
                        }
                        StepState.Complete -> {
                            Icon(Icons.Rounded.Check, null)
                        }
                        is StepState.Running -> {
                            CircularProgressIndicator()
                        }
                        is StepState.Error -> {
                            Icon(Icons.Rounded.Error, null)
                        }
                    }
                }
            }
        }
    )
}

@Preview
@Composable
private fun DownloadStepPreview() {
    PreviewTheme {
        Surface(color = MaterialTheme.colors.background) {
            Column {
                DownloadStep(remember {
                    DownloadStep(
                        1,
                        mutableStateOf(DownloadStep.State.Waiting)
                    )
                })
                DownloadStep(remember {
                    DownloadStep(
                        2,
                        mutableStateOf(
                            DownloadStep.State.Running(
                                mutableStateOf(20_000_000),
                                125_000_000
                            )
                        )
                    )
                })
                DownloadStep(remember {
                    DownloadStep(
                        3,
                        mutableStateOf(DownloadStep.State.Error("Error"))
                    )
                })
                DownloadStep(remember {
                    DownloadStep(
                        4,
                        mutableStateOf(DownloadStep.State.Complete)
                    )
                })
            }
        }
    }
}