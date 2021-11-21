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
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.theme.PreviewTheme
import org.wvt.horizonmgr.utils.longSizeToString
import org.wvt.horizonmgr.viewmodel.InstallPackageViewModel.DownloadStep
import org.wvt.horizonmgr.viewmodel.InstallPackageViewModel.StepState

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun StepItem(
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    trailing: @Composable () -> Unit
) {
    ListItem(
        modifier = Modifier.height(72.dp),
        icon = icon, text = text, trailing = trailing
    )
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DownloadStep(data: DownloadStep) {
    val downloadState by data.state
    val contentColor = when (downloadState) {
        is DownloadStep.State.Waiting -> MaterialTheme.colors.onBackground.copy(ContentAlpha.disabled)
        is DownloadStep.State.Error -> MaterialTheme.colors.error
        else -> MaterialTheme.colors.onBackground
    }
    StepItem(
        icon = {
            Icon(Icons.Default.CloudDownload, null)
        }, text = {
            when (val downloadState = downloadState) {
                DownloadStep.State.Waiting -> Text(text = "等待下载", color = contentColor)
                DownloadStep.State.Complete -> Text(text = "下载完成", color = contentColor)
                is DownloadStep.State.Running -> {
                    val downloaded = remember(downloadState.progress.value) {
                        longSizeToString(downloadState.progress.value)
                    }
                    val total = remember { longSizeToString(downloadState.total) }
                    Text(text = "正在下载（$downloaded / $total）", color = contentColor)
                }
                is DownloadStep.State.Error -> Text(text = "下载失败", color = contentColor)
            }

        }, trailing = {
            Crossfade(targetState = downloadState) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    when (it) {
                        DownloadStep.State.Waiting -> {
                        }
                        DownloadStep.State.Complete -> {
                            Icon(Icons.Default.Check, null)
                        }
                        is DownloadStep.State.Running -> {
                            CircularProgressIndicator(
                                progress = animateFloatAsState(it.progress.value.toFloat() / it.total.toFloat()).value
                            )
                        }
                        is DownloadStep.State.Error -> {
                            var showError by remember { mutableStateOf(false) }
                            IconButton(onClick = { showError = true }) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colors.error
                                )
                            }
                            if (showError) AlertDialog(
                                onDismissRequest = { showError = false },
                                title = { Text("Error detail") },
                                text = { Text(it.message) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showError = false
                                    }) { Text("确认") }
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
            Icon(Icons.Default.Inbox, null)
        }, text = {
            Text(
                text = when (mergeState) {
                    StepState.Waiting -> "等待合并"
                    StepState.Complete -> "合并完成"
                    is StepState.Running -> "正在合并"
                    is StepState.Error -> "合并失败"
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
                            Icon(Icons.Default.Check, null)
                        }
                        is StepState.Running -> {
                            CircularProgressIndicator()
                        }
                        is StepState.Error -> {
                            Icon(Icons.Default.Error, null)
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
            Icon(Icons.Default.GetApp, null)
        }, text = {
            Text(
                text = when (installState) {
                    StepState.Waiting -> "等待安装"
                    StepState.Complete -> "安装完成"
                    is StepState.Running -> "正在安装"
                    is StepState.Error -> "安装失败"
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
                            Icon(Icons.Default.Check, null)
                        }
                        is StepState.Running -> {
                            CircularProgressIndicator()
                        }
                        is StepState.Error -> {
                            Icon(Icons.Default.Error, null)
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