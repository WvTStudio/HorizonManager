package org.wvt.horizonmgr.ui.onlineinstall

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.theme.PreviewTheme

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
            ChoosePackageItem(
                onClick = { onChoose(index) },
                isRecommended = item.recommended,
                packageName = item.name,
                version = item.version
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChoosePackageItem(
    onClick: () -> Unit,
    isRecommended: Boolean,
    packageName: String,
    version: String,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        icon = {
            Icon(
                modifier = Modifier.padding(top = 4.dp),
                imageVector = Icons.Rounded.Extension,
                contentDescription = "安装"
            )
        },
        text = { Text(if (isRecommended) "${packageName}（推荐）" else packageName) },
        secondaryText = { Text(version) },
        trailing = {
            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = "选择"
            )
        }
    )
}

@Preview
@Composable
private fun Preview() = PreviewTheme {
    Surface(color = MaterialTheme.colors.background) {
        Column {
            ChoosePackageItem(
                onClick = { },
                isRecommended = true,
                packageName = "Example package",
                version = "v1.2.3"
            )
            ChoosePackageItem(
                onClick = { },
                isRecommended = false,
                packageName = "Example package",
                version = "v1.2.3"
            )
        }
    }
}