package org.wvt.horizonmgr.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun InstallMCDialog(
    onDismissClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onNeverShowClick: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.shadow(16.dp, clip = false),
        onDismissRequest = onDismissClick,
        buttons = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                Row(Modifier.weight(1f)) {
                    TextButton(onClick = onNeverShowClick) { Text("不再显示") }
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDismissClick) { Text("取消") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onConfirmClick) { Text("前往 Google Play") }
            }
        },
        title = { Text("您还未安装 Minecraft 游戏本体") },
        text = {
            Text("Horizon 已内嵌 Minecraft，但需要安装 Minecraft 本体以验证您是否为正版玩家")
        }
    )
}