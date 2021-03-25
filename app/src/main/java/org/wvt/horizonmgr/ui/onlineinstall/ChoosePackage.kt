package org.wvt.horizonmgr.ui.onlineinstall

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Extension
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class ChoosePackageItem(
    val uuid: String,
    val name: String,
    val version: String,
    val recommended: Boolean
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChoosePackage(
    modifier: Modifier,
    items: List<ChoosePackageItem>,
    onChoose: (index: Int) -> Unit
) {
    LazyColumn(modifier) {
        itemsIndexed(items) { index, item ->
            ListItem(
                modifier = Modifier.clickable(onClick = { onChoose(index) }),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Extension,
                        contentDescription = "安装"
                    )
                },
                text = { Text(if (item.recommended) "${item.name}（推荐）" else item.name) },
                secondaryText = { Text(item.version) },
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