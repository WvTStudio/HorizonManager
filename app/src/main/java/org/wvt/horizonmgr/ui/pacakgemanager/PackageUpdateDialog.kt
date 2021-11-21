package org.wvt.horizonmgr.ui.pacakgemanager

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.wvt.horizonmgr.viewmodel.PackageManagerViewModel

/**
 * 更新界面
 * 步骤：
 *  1. 获取信息
 *  2. 下载
 *  3. 安装
 *  4. 完成
 * 向用户显示当前步骤
 * 显示下载进度，显示安装进度（如果代码可行）
 * 下载错误提供重试方式
 */
@Composable
fun PackageUpdateDialog(viewModel: PackageManagerViewModel) {
    var display by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel.updateState) {
        display = viewModel.updateState != null
    }
    viewModel.updateState?.let { updateState ->
        if (display) Dialog(onDismissRequest = {
            if (updateState == PackageManagerViewModel.UpdateState.Succeed || updateState == PackageManagerViewModel.UpdateState.Failed) {
                display = false
            }
        }) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Box(Modifier.padding(16.dp)) {
                    when (updateState) {
                        PackageManagerViewModel.UpdateState.Parsing -> {
                            Text("正在解析")
                        }
                        is PackageManagerViewModel.UpdateState.Downloading -> Row {
                            Text("正在下载")
                            CircularProgressIndicator(updateState.progress.value)
                        }
                        PackageManagerViewModel.UpdateState.Installing -> {
                            Text("正在安装")
                        }
                        PackageManagerViewModel.UpdateState.Succeed -> {
                            Text("安装成功")
                        }
                        PackageManagerViewModel.UpdateState.Failed -> {
                            Text("更新失败")
                        }
                    }
                }
            }
        }
    }
}