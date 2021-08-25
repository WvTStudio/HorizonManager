package org.wvt.horizonmgr.ui.pacakgemanager

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.wvt.horizonmgr.ui.theme.PreviewTheme

/**
 * 分享界面
 * 让用户输入打包的文件名
 * 开始打包
 * 显示打包状态
 * 成功后显示打包的文件路径、大小
 *
 */
@Composable
fun SharePackageDialog() {
    // TODO: 2021/6/21 等待 UI 设计师
    var display by remember { mutableStateOf(false) }
    if (display) Dialog(onDismissRequest = { display = false }) {
        Card(
            Modifier
                .animateContentSize()
                .sizeIn(minWidth = 200.dp, minHeight = 400.dp), elevation = 16.dp) {
            PreparingState()
        }
    }
    Column {
        Button(onClick = {
            display = true
        }) {
            Text("Display")
        }
    }
}

@Composable
fun PreparingState() {
    Column {
        Text("Preparing")
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        SharePackageDialog()
    }
}