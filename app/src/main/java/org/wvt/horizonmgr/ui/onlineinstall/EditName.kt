package org.wvt.horizonmgr.ui.onlineinstall

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

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
            label = { Text("分包名称") })

        Row(verticalAlignment = Alignment.CenterVertically) {
            ListItem(
                modifier = Modifier.weight(1f),
                icon = { Icon(modifier = Modifier.padding(top = 4.dp),imageVector = Icons.Filled.Extension, contentDescription = "信息") },
                text = { Text(name) },
                secondaryText = { Text(version) }
            )
            Button(
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = { onConfirm(customName.text) }
            ) { Text("下一步") }
        }
    }
}