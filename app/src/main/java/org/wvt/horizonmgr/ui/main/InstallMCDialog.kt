package org.wvt.horizonmgr.ui.main

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@Composable
fun InstallMCDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        title = { Text("您还未安装 Minecraft 游戏本体") },
        text = {
            Text("尽管 Horizon 已经内嵌了 Minecraft，但需要您安装游戏本体，以验证您是否为正版玩家。")
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("前往 Google Play") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}