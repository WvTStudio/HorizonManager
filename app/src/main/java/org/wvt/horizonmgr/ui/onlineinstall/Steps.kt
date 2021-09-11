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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StepItem(
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
        is StepState.Waiting -> MaterialTheme.colors.onBackground.copy(ContentAlpha.disabled)
        else -> MaterialTheme.colors.onBackground
    }
    StepItem(
        icon = {
            Icon(Icons.Default.CloudDownload, null)
        }, text = {
            Text(
                text = when (downloadState) {
                    StepState.Waiting -> "等待下载"
                    StepState.Complete -> "下载完成"
                    is StepState.Running -> "正在下载"
                    is StepState.Error -> "下载失败"
                },
                color = contentColor
            )
        }, trailing = {
            Crossfade(targetState = downloadState) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    when (it) {
                        StepState.Waiting -> {
                        }
                        StepState.Complete -> {
                            Icon(Icons.Default.Check, null)
                        }
                        is StepState.Running -> {
                            CircularProgressIndicator(
                                progress = animateFloatAsState(it.progress.value).value
                            )
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
                DownloadStep(remember { DownloadStep(1, mutableStateOf(StepState.Waiting)) })
                DownloadStep(remember {
                    DownloadStep(
                        2,
                        mutableStateOf(StepState.Running(mutableStateOf(0.2f)))
                    )
                })
                DownloadStep(remember { DownloadStep(3, mutableStateOf(StepState.Error("Error"))) })
                DownloadStep(remember { DownloadStep(4, mutableStateOf(StepState.Complete)) })
            }
        }
    }
}