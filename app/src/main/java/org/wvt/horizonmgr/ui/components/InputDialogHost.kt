package org.wvt.horizonmgr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
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
        InputDialog(
            onDismissRequest = { data.cancel() },
            title = data.title,
            text = data.text.value,
            onTextChange = { data.text.value = it },
            label = data.inputLabel,
            onConfirmEnabled = data.text.value.text.isNotBlank(),
            onCancelClick = { data.cancel() },
            onConfirmClick = { data.confirm(data.text.value.text) }
        )
    }
}

@Composable
fun InputDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    label: String,
    onConfirmEnabled: Boolean,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(elevation = 16.dp) {
            Column {
                Text(
                    modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp),
                    text = title, style = MaterialTheme.typography.h6
                )
                TextField(
                    modifier = Modifier
                        .padding(top = 16.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth(),
                    value = text,
                    onValueChange = onTextChange,
                    label = { Text(label) })
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
                        onClick = onCancelClick
                    ) { Text("取消") }
                    TextButton(
                        onClick = onConfirmClick,
                        enabled = onConfirmEnabled
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun InputDialogHostPreview() {
    InputDialog(
        onDismissRequest = {},
        title = "Example",
        text = remember { TextFieldValue("Example") },
        onTextChange = {},
        label = "Example",
        onCancelClick = {},
        onConfirmEnabled = true,
        onConfirmClick = {}
    )
}