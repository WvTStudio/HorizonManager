package org.wvt.horizonmgr.ui.`package`

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Extension
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

data class ChoosePackageItem(val name: String, val version: String, val recommended: Boolean)

@Composable
fun ChoosePackage(
    items: List<ChoosePackageItem>,
    onCancel: () -> Unit,
    onChosen: (index: Int) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.Filled.ArrowBack) } },
            title = { Text("在线安装分包") }, backgroundColor = MaterialTheme.colors.surface
        )
        if (items.isEmpty()) {
            Stack(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        } else {
            items.forEachIndexed { index, it ->
                ListItem(
                    modifier = Modifier.clickable(onClick = { onChosen(index) }),
                    icon = { Icon(asset = Icons.Filled.Extension) },
                    text = { Text(if (it.recommended) "${it.name}（推荐）" else it.name) },
                    secondaryText = { Text(it.version) },
                    trailing = { Icon(asset = Icons.Filled.ArrowForward) }
                )
            }
        }
    }
}

@Composable
fun EditName(item: ChoosePackageItem, onCancel: () -> Unit, onConfirm: (name: String) -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue(text = "Inner Core")) }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onCancel) { Icon(Icons.Filled.ArrowBack) }
            },
            title = {
                Text("输入分包的名字")
            }, backgroundColor = MaterialTheme.colors.surface
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
            value = name,
            onValueChange = { name = it },
            label = { Text("分包名称") })

        Row(verticalAlignment = Alignment.CenterVertically) {
            ListItem(
                modifier = Modifier.weight(1f),
                icon = { Icon(asset = Icons.Filled.Extension) },
                text = { Text(item.name) },
                secondaryText = { Text(item.version) }
            )
            Button(
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = { onConfirm(name.text) }
            ) { Text("下一步") }
        }
    }
}