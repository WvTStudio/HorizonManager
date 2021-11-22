package org.wvt.horizonmgr.ui.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R

@Composable
fun ErrorBanner(modifier: Modifier, errors: List<String>, text: String) {
    var visible by remember(errors) { mutableStateOf(errors.isNotEmpty()) }
    var displayDetail by remember { mutableStateOf(false) }
    if (displayDetail) {
        AlertDialog(
            modifier = Modifier.shadow(16.dp, clip = false),
            title = { Text(stringResource(R.string.error_banner_title)) },
            text = {
                Text(text = remember(errors) {
                    errors.foldIndexed("") { index, acc, e ->
                        "$acc[Error $index]: $e\n\n"
                    }
                })
            },
            confirmButton = {
                TextButton(onClick = { displayDetail = false }) {
                    Text(stringResource(R.string.button_action_confirm))
                }
            },
            onDismissRequest = { displayDetail = false }
        )
    }
    MaterialBanner(modifier = modifier, visible = visible, text = {
        Text(text)
    }, dismissButton = {
        TextButton(onClick = { visible = false }) {
            Text(stringResource(R.string.error_banner_action_close))
        }
    }, confirmButton = {
        TextButton(onClick = { displayDetail = true }) {
            Text(stringResource(R.string.error_banner_action_detail))
        }
    })
}