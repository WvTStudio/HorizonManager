package org.wvt.horizonmgr.ui.onlineinstall

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.theme.PreviewTheme


/**
 * 分包经过解析、分区块下载、合并区块、安装几个步骤
 */
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun InstallProgress(
    totalProgress: Float,
    downloadSteps: List<InstallPackageViewModel.DownloadStep>,
    mergeState: InstallPackageViewModel.StepState,
    installState: InstallPackageViewModel.StepState,
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
                    DownloadStep(remember { InstallPackageViewModel.DownloadStep(index, item.state)})
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
