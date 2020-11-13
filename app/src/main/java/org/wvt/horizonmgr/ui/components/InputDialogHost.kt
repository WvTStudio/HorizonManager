package org.wvt.horizonmgr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

class InputDialogHostState {
    private val mutex = Mutex()
    var currentData by mutableStateOf<Data?>(null)

    suspend fun showDialog(title: String, inputLabel: String): DialogResult {
        return mutex.withLock {
            try {
                suspendCancellableCoroutine<DialogResult> {
                    currentData = Data(title, inputLabel, it)
                }
            } finally {
                currentData = null
            }
        }
    }

    class Data(
        val title: String,
        val inputLabel: String,
        private val continuation: CancellableContinuation<DialogResult>
    ) {
        fun cancel() {
            continuation.resume(DialogResult.Canceled)
        }

        fun confirm(value: String) {
            continuation.resume(DialogResult.Confirm(value))
        }
    }

    sealed class DialogResult {
        object Canceled : DialogResult()
        class Confirm(val name: String) : DialogResult()
    }
}

@Composable
fun InputDialogHost(state: InputDialogHostState) {
    val data = state.currentData
    var value by remember { mutableStateOf(TextFieldValue()) }
    if (data != null) {
        Dialog(onDismissRequest = {
            data.cancel()
        }) {
            Card(elevation = 16.dp) {
                Column {
                    Text(
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        text = data.title, style = MaterialTheme.typography.h6
                    )
                    TextField(
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                        value = value,
                        onValueChange = { value = it },
                        label = { Text(data.inputLabel) })
                    Row(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 8.dp
                        ).fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            modifier = Modifier.padding(end = 8.dp),
                            onClick = { data.cancel() }
                        ) { Text("取消") }
                        TextButton(onClick = {
                            data.confirm(value.text)
                        }, enabled = value.text.isNotBlank()) { Text("确定") }
                    }
                }
            }
        }
    }
}
