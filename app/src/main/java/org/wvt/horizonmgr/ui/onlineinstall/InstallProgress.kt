package org.wvt.horizonmgr.ui.onlineinstall

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.theme.PreviewTheme

sealed class StepState {
    object Waiting : StepState()
    object Complete : StepState()
    class Running(val progress: State<Float>) : StepState()
    class Error(val message: String) : StepState()
}

data class DownloadStep(
    val chunk: Int,
    val state: StepState
)

/**
 * 分包经过解析、分区块下载、合并区块、安装几个步骤
 */
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun InstallProgress(
    totalProgress: Float,
    downloadSteps: List<DownloadStep>,
    mergeState: StepState,
    installState: StepState,
    onCancelClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // Total Progress
            AnimatedVisibility(
                visible = totalProgress < 1f,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(
                    progress = animateFloatAsState(totalProgress).value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                )
            }
            // Steps
            LazyColumn(
                Modifier
                    .fillMaxSize()
            ) {
                itemsIndexed(downloadSteps) { index, item ->
                    val state = item.state
                    DownloadStep(state)
                }
                item {
                    MergeStep(mergeState)
                }
                item {
                    InstallStep(installState)
                }
            }
        }
        AnimatedVisibility(
            visible = totalProgress >= 1f, enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Button(onClick = onCompleteClick) {
                Text("完成")
            }
        }
    }
}

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


@Composable
fun DownloadStep(downloadState: StepState) {
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

@Composable
fun InstallStep(installState: StepState) {
    val contentColor = when (installState) {
        is StepState.Waiting -> MaterialTheme.colors.onBackground.copy(ContentAlpha.disabled)
        else -> MaterialTheme.colors.onBackground
    }
    StepItem(
        icon = {
            Icon(Icons.Default.Inbox, null)
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
            Crossfade(targetState = installState) {
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
                DownloadStep(StepState.Waiting)
                DownloadStep(StepState.Running(remember { mutableStateOf(0.2f) }))
                DownloadStep(StepState.Error("Error"))
                DownloadStep(StepState.Complete)
            }
        }
    }
}