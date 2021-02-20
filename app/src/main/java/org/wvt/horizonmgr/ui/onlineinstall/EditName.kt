package org.wvt.horizonmgr.ui.onlineinstall

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Extension
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EditName(
    name: String,
    version: String,
    onCancel: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    var customName by remember { mutableStateOf(TextFieldValue(text = "$name $version")) }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            title = {
                Text("输入分包的名字")
            }, backgroundColor = MaterialTheme.colors.surface
        )
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
                icon = { Icon(imageVector = Icons.Filled.Extension, contentDescription = "信息") },
                text = { Text(name) },
                secondaryText = { Text(version) },
                singleLineSecondaryText = true
            )
            Button(
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = { onConfirm(customName.text) }
            ) { Text("下一步") }
        }
    }
}