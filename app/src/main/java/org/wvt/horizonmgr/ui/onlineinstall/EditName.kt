package org.wvt.horizonmgr.ui.onlineinstall

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EditName(
    modifier: Modifier,
    name: String,
    version: String,
    onConfirm: (name: String) -> Unit
) {
    var customName by remember { mutableStateOf(TextFieldValue(text = "$name $version")) }
    Column(modifier) {
        TextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .fillMaxWidth(),
            value = customName,
            onValueChange = { customName = it },
            label = { Text(stringResource(R.string.ol_install_screen_edit_name_label)) })

        Row(verticalAlignment = Alignment.CenterVertically) {
            ListItem(
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        modifier = Modifier.padding(top = 4.dp),
                        imageVector = Icons.Rounded.Extension,
                        contentDescription = stringResource(R.string.ol_install_screen_edit_info)
                    )
                },
                text = { Text(name) },
                secondaryText = { Text(version) }
            )
            Button(
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = { onConfirm(customName.text) }
            ) { Text(stringResource(R.string.ol_install_screen_action_next)) }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        EditName(modifier = Modifier.wrapContentHeight(), name = "Example Package", version = "example version", onConfirm = {})
    }
}