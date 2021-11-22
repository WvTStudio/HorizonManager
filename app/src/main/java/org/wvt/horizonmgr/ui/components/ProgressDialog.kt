package org.wvt.horizonmgr.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

sealed class ProgressDialogState(val message: String) {
    class Loading(message: String) : ProgressDialogState(message)
    class ProgressLoading(message: String, val progress: Float) : ProgressDialogState(message)
    class Failed(val title: String, message: String, val detail: String? = null) : ProgressDialogState(message)
    class Finished(message: String) : ProgressDialogState(message)
}

@Composable
fun ProgressDialog(
    onCloseRequest: () -> Unit,
    state: ProgressDialogState
) {
    Dialog(onDismissRequest = {
        // 加载过程中不可关闭
        if (state is ProgressDialogState.Failed || state is ProgressDialogState.Finished)
            onCloseRequest()
    }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (state) {
                    is ProgressDialogState.Loading -> {
                        CircularProgressIndicator()
                        Content(state.message)
                    }
                    is ProgressDialogState.ProgressLoading -> {
                        CircularProgressIndicator(animateFloatAsState(state.progress).value)
                        Content(state.message)
                    }
                    is ProgressDialogState.Failed -> {
                        Box(Modifier.size(40.dp), Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                tint = MaterialTheme.colors.error,
                                contentDescription = "错误"
                            )
                        }
                        Content(state.message)
                    }
                    is ProgressDialogState.Finished -> {
                        Box(Modifier.size(40.dp), Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                tint = MaterialTheme.colors.secondary,
                                contentDescription = "成功"
                            )
                        }
                        Content(state.message)
                    }
                }
            }
        }
    }
}

@Composable
private fun Content(content: String) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
        Text(
            modifier = Modifier.padding(start = 32.dp, end = 16.dp),
            text = content
        )
    }
}