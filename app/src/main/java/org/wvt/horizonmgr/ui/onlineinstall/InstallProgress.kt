package org.wvt.horizonmgr.ui.onlineinstall

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor

sealed class StepState {
    @Stable
    object Waiting : StepState()

    @Stable
    object Complete : StepState()

    @Stable
    class Running(val progress: State<Float>) : StepState()

    @Stable
    class Error(val error: Throwable) : StepState()
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun InstallProgress(
    totalProgress: Float,
    downloadState: StepState,
    installState: StepState,
    onCancelClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(title = {
                Crossfade(if (totalProgress >= 1f) "安装完成" else "正在安装") {
                    Text(it)
                }
            }, navigationIcon = {
                // 安装目前不可被取消
            }, backgroundColor = AppBarBackgroundColor)
            Row(
                Modifier
                    .height(2.dp)
                    .fillMaxWidth()
            ) {
                AnimatedVisibility(
                    visible = totalProgress < 1f,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LinearProgressIndicator(
                        progress = animateFloatAsState(totalProgress).value,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    val contentColor = when (downloadState) {
                        is StepState.Waiting -> MaterialTheme.colors.onBackground.copy(ContentAlpha.disabled)
                        else -> MaterialTheme.colors.onBackground
                    }
                    ListItem(
                        modifier = Modifier.height(72.dp),
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
                            Box(Modifier.width(48.dp)) {
                                Crossfade(
                                    modifier = Modifier.align(Alignment.Center),
                                    targetState = downloadState
                                ) {
                                    when (it) {
                                        StepState.Waiting -> { }
                                        StepState.Complete -> {
                                            Icon(Icons.Default.Check, contentDescription = "下载完成")
                                        }
                                        is StepState.Running -> {
                                            CircularProgressIndicator(
                                                progress = animateFloatAsState(it.progress.value).value
                                            )
                                        }
                                        is StepState.Error -> {
                                            Icon(Icons.Default.Error, contentDescription = "下载失败")
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
                item {
                    val contentColor = when (installState) {
                        is StepState.Waiting -> MaterialTheme.colors.onBackground.copy(ContentAlpha.disabled)
                        else -> MaterialTheme.colors.onBackground
                    }
                    ListItem(
                        modifier = Modifier.height(72.dp),
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
                            Box(Modifier.width(48.dp)) {
                                Crossfade(
                                    modifier = Modifier.align(Alignment.Center),
                                    targetState = installState
                                ) {
                                    when (it) {
                                        StepState.Waiting -> { }
                                        StepState.Complete -> {
                                            Icon(Icons.Default.Check, contentDescription = "安装完成")
                                        }
                                        is StepState.Running -> {
                                            CircularProgressIndicator()
                                        }
                                        is StepState.Error -> {
                                            Icon(Icons.Default.Error, contentDescription = "下载失败")
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = totalProgress >= 1f, enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Button(onClick = onCompleteClick) {
                Text("完成")
            }
        }
    }
}