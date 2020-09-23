package org.wvt.horizonmgr.ui.components

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.ui.tooling.preview.Preview
import org.wvt.horizonmgr.ui.theme.HorizonManagerTheme

sealed class ProgressDialogState {
    class Loading(val message: String) : ProgressDialogState()
    class ProgressLoading(val message: String, val progress: Float) : ProgressDialogState()
    class Failed(val title: String, val message: String) : ProgressDialogState()
    class Finished(val message: String) : ProgressDialogState()
}

@Composable
fun ProgressDialog(
    onCloseRequest: () -> Unit,
    state: ProgressDialogState
) {
    Dialog(onDismissRequest = {
        // 加载过程中不可关闭
        if (state is ProgressDialogState.Failed || state is ProgressDialogState.Finished) onCloseRequest()
    }) {
        Card(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (state) {
                    is ProgressDialogState.Loading -> {
                        CircularProgressIndicator()
                        Text(
                            modifier = Modifier.padding(start = 24.dp, end = 16.dp)
                                .fillMaxWidth(), text = state.message
                        )
                    }
                    is ProgressDialogState.ProgressLoading -> {
                        CircularProgressIndicator(state.progress)
                        Text(
                            modifier = Modifier.padding(start = 24.dp, end = 16.dp)
                                .fillMaxWidth(), text = state.message
                        )
                    }
                    is ProgressDialogState.Failed -> {
                        Icon(asset = Icons.Filled.Close, tint = MaterialTheme.colors.error)
                        Text(
                            text = state.message,
                            modifier = Modifier.padding(start = 24.dp, end = 16.dp)
                        )
                    }
                    is ProgressDialogState.Finished -> {
                        Icon(asset = Icons.Filled.Check, tint = MaterialTheme.colors.secondary)
                        Text(
                            text = state.message,
                            modifier = Modifier.padding(start = 24.dp, end = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ProgressDialogPreview() {
    val state by remember {
        mutableStateOf<ProgressDialogState>(ProgressDialogState.Loading("正在加载"))
    }
    HorizonManagerTheme {
        Surface {
            ProgressDialog(onCloseRequest = {}, state = state)
        }
    }
}