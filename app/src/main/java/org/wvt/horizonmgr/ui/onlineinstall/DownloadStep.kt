package org.wvt.horizonmgr.ui.onlineinstall

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.wvt.horizonmgr.ui.theme.PreviewTheme

/**
 * 展示当前下载的 Chunk
 * 显示下载状态：
 * 速度、总大小、已下载大小、预计剩余时间
 * 错误状态：
 * 错误原因，可重试
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DownloadStep(
    chunk: Int,
    speed: Int,
    downloaded: Long,
    total: Long
) {
    ListItem(
        icon = { Icon(Icons.Filled.Download, contentDescription = null) },
        trailing = {
            CircularProgressIndicator(progress = (downloaded.toDouble() / total).toFloat())
        },
        overlineText = {
            Text(text = "区块 $chunk")
        },
        text = {
            Text("正在下载")
        },
        secondaryText = {
            Text("${downloaded}B/${total}B ${speed}B/s")
        },
        singleLineSecondaryText = false
    )
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Surface(color = MaterialTheme.colors.background) {
            Column {
                DownloadStep(chunk = 1, speed = 1000, downloaded = 5120, total = 10240)
                DownloadStep(chunk = 2, speed = 1000, downloaded = 5120, total = 10240)
                DownloadStep(chunk = 3, speed = 1000, downloaded = 5120, total = 10240)
                DownloadStep(chunk = 4, speed = 1000, downloaded = 5120, total = 10240)
            }
        }
    }
}