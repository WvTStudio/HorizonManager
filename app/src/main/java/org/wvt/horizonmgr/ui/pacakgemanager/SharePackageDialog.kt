package org.wvt.horizonmgr.ui.pacakgemanager

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import org.wvt.horizonmgr.BuildConfig
import java.io.File


/**
 * 分享界面
 * 让用户输入打包的文件名
 * 开始打包
 * 显示打包状态
 * 成功后显示打包的文件路径、大小
 *
 */
@Composable
fun SharePackageDialog(viewModel: PackageManagerViewModel) {
    var display by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel.sharePackageState) {
        display = viewModel.sharePackageState != null
    }
    if (display) Dialog(onDismissRequest = {
        if (viewModel.sharePackageState is PackageManagerViewModel.SharePackageState.Succeed || viewModel.sharePackageState == PackageManagerViewModel.SharePackageState.Failed) {
            display = false
        }
    }) {
        Card(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(), elevation = 16.dp
        ) {
            when (val state = viewModel.sharePackageState) {
                PackageManagerViewModel.SharePackageState.EditName -> Column(
                    Modifier.padding(16.dp),
                    Arrangement.spacedBy(16.dp)
                ) {
                    Text("输入名称")
                    var input by remember { mutableStateOf("") }
                    TextField(value = input, onValueChange = { input = it })
                    Button(
                        modifier = Modifier.align(Alignment.End),
                        onClick = { viewModel.setShareName(input) }) {
                        Text("确定")
                    }
                }
                PackageManagerViewModel.SharePackageState.Failed -> Row(Modifier.padding(16.dp)) {
                    Text("打包失败")
                }
                PackageManagerViewModel.SharePackageState.Processing -> Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(modifier = Modifier.weight(1f), text = "正在打包")
                    CircularProgressIndicator()
                }
                is PackageManagerViewModel.SharePackageState.Succeed -> Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(modifier = Modifier.weight(1f), text = "打包成功")
                    val context = LocalContext.current

                    Button(onClick = {
                        share(context, state.file)
                    }) {
                        Text("分享")
                    }
                }
                null -> {
                }
            }
        }
    }
}

private fun share(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "application/zip"
        putExtra(Intent.EXTRA_STREAM, uri)
    }
    context.startActivity(Intent.createChooser(sendIntent, "将分包分享至："))
}