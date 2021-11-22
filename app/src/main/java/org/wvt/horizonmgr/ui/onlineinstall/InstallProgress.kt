package org.wvt.horizonmgr.ui.onlineinstall

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.viewmodel.InstallPackageViewModel


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
                Text(stringResource(R.string.ol_install_screen_button_complete))
            }
        }
    }
}
