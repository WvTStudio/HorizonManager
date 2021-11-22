package org.wvt.horizonmgr.ui.onlineinstall

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.theme.PreviewTheme

enum class State {
    WAITING, DOWNLOADING, FAILED, COMPLETED
}

/**
 * 展示当前下载的 Chunk
 * 显示下载状态：
 * 速度、总大小、已下载大小、预计剩余时间
 * 错误状态：
 * 错误原因，可重试
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun NewDownloadStep(
    chunk: Int,
    speed: Int,
    downloaded: Long,
    total: Long,
    state: State
) {
    ListItem(
        icon = { Icon(Icons.Rounded.Download, contentDescription = null) },
        overlineText = {
            Text(text = "区块 $chunk")
        },
        text = {
            when (state) {
                State.WAITING -> Text("等待下载")
                State.DOWNLOADING -> Text("正在下载")
                State.FAILED -> Text("下载失败")
                State.COMPLETED -> Text("下载完成")
            }
        },
        secondaryText = {
            Text("${downloaded}B/${total}B ${speed}B/s")
        },
        trailing = {
            AnimatedContent(targetState = state) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    when (it) {
                        State.WAITING -> CircularProgressIndicator()
                        State.DOWNLOADING -> CircularProgressIndicator(progress = (downloaded.toDouble() / total).toFloat())
                        State.FAILED -> Icon(Icons.Rounded.Error, "Failed")
                        State.COMPLETED -> Icon(Icons.Rounded.Check, "Completed")
                    }
                }
            }
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
                NewDownloadStep(chunk = 1, speed = 1000, downloaded = 5120, total = 10240, State.DOWNLOADING)
                NewDownloadStep(chunk = 2, speed = 1000, downloaded = 5120, total = 10240, State.WAITING)
                NewDownloadStep(chunk = 3, speed = 1000, downloaded = 5120, total = 10240, State.COMPLETED)
                NewDownloadStep(chunk = 4, speed = 1000, downloaded = 5120, total = 10240, State.FAILED)
            }
        }
    }
}