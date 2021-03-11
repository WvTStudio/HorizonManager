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
        private set

    suspend fun showDialog(
        defaultValue: String = "",
        title: String,
        inputLabel: String
    ): DialogResult = mutex.withLock {
        try {
            return suspendCancellableCoroutine {
                currentData =
                    Data(mutableStateOf(TextFieldValue(defaultValue)), title, inputLabel, it)
            }
        } finally {
            currentData = null
        }
    }

    class Data(
        val text: MutableState<TextFieldValue>,
        val title: String,
        val inputLabel: String,
        private val continuation: CancellableContinuation<DialogResult>
    ) {
        fun cancel() {
            if (continuation.isActive) continuation.resume(DialogResult.Canceled)
        }

        fun confirm(value: String) {
            if (continuation.isActive) continuation.resume(DialogResult.Confirm(value))
        }
    }

    @Stable
    sealed class DialogResult {
        @Stable
        object Canceled : DialogResult()

        @Stable
        data class Confirm(val input: String) : DialogResult()
    }
}

@Composable
fun InputDialogHost(state: InputDialogHostState) {
    val data = state.currentData

    if (data != null) {
        Dialog(onDismissRequest = { data.cancel() }) {
            Card(
                elevation = 24.dp
            ) {
                Column {
                    Text(
                        modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp),
                        text = data.title, style = MaterialTheme.typography.h6
                    )
                    TextField(
                        modifier = Modifier
                            .padding(top = 16.dp, start = 24.dp, end = 24.dp)
                            .fillMaxWidth(),
                        value = data.text.value,
                        onValueChange = { data.text.value = it },
                        label = { Text(data.inputLabel) })
                    Row(
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 8.dp
                            )
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            modifier = Modifier.padding(end = 8.dp),
                            onClick = { data.cancel() }
                        ) { Text("取消") }
                        TextButton(onClick = {
                            data.confirm(data.text.value.text)
                        }, enabled = data.text.value.text.isNotBlank()) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}
