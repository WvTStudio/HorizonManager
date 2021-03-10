package org.wvt.horizonmgr.ui.onlineinstall

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
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor

data class ChoosePackageItem(
    val uuid: String,
    val name: String,
    val version: String,
    val recommended: Boolean
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChoosePackage(
    items: List<ChoosePackageItem>,
    onCancel: () -> Unit,
    onChoose: (index: Int) -> Unit
) {
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
            title = { Text("在线安装分包") }, backgroundColor = AppBarBackgroundColor
        )
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        } else {
            items.forEachIndexed { index, it ->
                ListItem(
                    modifier = Modifier.clickable(onClick = { onChoose(index) }),
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Extension,
                            contentDescription = "安装"
                        )
                    },
                    text = { Text(if (it.recommended) "${it.name}（推荐）" else it.name) },
                    secondaryText = { Text(it.version) },
                    trailing = {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "选择"
                        )
                    }
                )
            }
        }
    }
}