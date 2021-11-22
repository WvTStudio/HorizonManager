package org.wvt.horizonmgr.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R

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
                    .padding(8.dp)
            ) {
                Row(Modifier.weight(1f)) {
                    TextButton(onClick = onNeverShowClick) {
                        Text(stringResource(R.string.install_mc_dialog_hide_forever))
                    }
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDismissClick) {
                    Text(stringResource(R.string.install_mc_dialog_cancel))
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onConfirmClick) {
                    Text(stringResource(R.string.install_mc_dialog_confirm))
                }
            }
        },
        title = { Text(stringResource(R.string.install_mc_dialog_title)) },
        text = {
            Text(stringResource(R.string.install_mc_dialog_text))
        }
    )
}