package org.wvt.horizonmgr.ui.pacakgemanager

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.legacyservice.WebAPI
import org.wvt.horizonmgr.ui.AmbientHorizonManager
import org.wvt.horizonmgr.ui.AmbientWebAPI

private data class Step(
    val icon: ImageVector,
    val label: String,
    val progressable: Boolean
)

// TODO 反正是要改的 屎山就屎山吧
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InstallPackage(packInfo: WebAPI.ICPackage, name: String, onFinished: () -> Unit) {
    // TODO: 2020/11/5 使用 ViewModel 重写 
    var totalProgress by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    val horizonMgr = AmbientHorizonManager.current
    val webApi = AmbientWebAPI.current

    val steps = remember {
        listOf(
            Step(Icons.Filled.CloudDownload, "正在下载", true),
            Step(Icons.Filled.Inbox, "正在安装", false),
        )
    }

    var currentStep by remember { mutableStateOf(0) }
    var currentProgress by remember { mutableStateOf(0f) }
    var currentStepState by remember { mutableStateOf(0) } // 0: todoit, 1: doing, 2: failed

    DisposableEffect(Unit) {
        scope.launch {
            val task = webApi.downloadPackage(packInfo)
            task.progressChannel().receiveAsFlow().conflate().collect {
                delay(200)
                currentProgress = it
            }
            val result = try {
                task.await()
            } catch (e: Exception) {
                currentStepState = 2
                e.printStackTrace()
                return@launch
            }
            totalProgress = 0.5f
            currentStep++
            delay(500)

            try {
                horizonMgr.installPackage(name, result.first, result.second, packInfo.uuid)
            } catch (e: Exception) {
                currentStepState = 2
                e.printStackTrace()
                return@launch
            }

            delay(500)
            totalProgress = 1f
            currentStep++
        }
        onDispose {
            // TODO: 2021/2/7 添加 Cancel 逻辑
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(title = {
                Crossfade(if (totalProgress >= 1f) "安装完成" else "正在安装") {
                    Text(it)
                }
            }, navigationIcon = {
                // 安装目前不可被取消
            }, backgroundColor = MaterialTheme.colors.surface)
            Row(
                Modifier
                    .height(2.dp)
                    .fillMaxWidth()) {
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
                itemsIndexed(steps) { index, step ->
                    val state = when {
                        index == currentStep && currentStepState == 2 -> -1
                        index < currentStep -> 0 // finished
                        index == currentStep -> 1 // doing
                        index > currentStep -> 2 // todoit
                        else -> -1
                    }
                    val contentColor = animateColorAsState(
                        if (state == 2) AmbientContentColor.current.copy(alpha = 0.5f)
                        else MaterialTheme.colors.onSurface
                    ).value
                    ListItem(
                        modifier = Modifier.height(72.dp),
                        icon = {
                            Icon(step.icon, null, Modifier, contentColor)
                        }, text = {
                            Text(
                                text = step.label,
                                color = contentColor
                            )
                        }, trailing = {
                            Box(Modifier.width(48.dp)) {
                                Crossfade(
                                    current = state,
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    if (state == 1) {
                                        // Doing
                                        if (step.progressable)
                                            CircularProgressIndicator(
                                                animateFloatAsState(
                                                    currentProgress
                                                ).value)
                                        else CircularProgressIndicator()
                                    } else if (state == 0) {
                                        // Finished
                                        Icon(Icons.Filled.Check, "成功")
                                    } else if (state == -1) {
                                        Icon(Icons.Filled.Error, "失败")
                                    }
                                    // Todoit - Nothing
                                }
                            }
                        }
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = totalProgress >= 1f, enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Button(onClick = onFinished) {
                Text("完成")
            }
        }
    }
}