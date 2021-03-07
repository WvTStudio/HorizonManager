package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.components.LocalImage
import org.wvt.horizonmgr.ui.theme.HorizonManagerTheme

@Composable
internal fun LevelItem(
    modifier: Modifier = Modifier,
    levelName: String,
    screenshot: String?,
    onRenameClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onCopyClick: () -> Unit,
    onMoveClick: () -> Unit
) {
    var dropdownExpand by remember { mutableStateOf(false) }

    Card(modifier = modifier.aspectRatio(17f / 9f)) {
        Box(Modifier.fillMaxSize()) {
            LocalImage(
                modifier = Modifier
                    .matchParentSize(),
                path = screenshot, contentDescription = "存档预览图",
                contentScale = ContentScale.Crop
            )
            Text(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart),
                text = levelName,
                style = MaterialTheme.typography.h6,
                color = Color.White
            )
            Box(
                Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomEnd)
            ) {
                IconButton(
                    onClick = { dropdownExpand = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "选项",
                        tint = Color.White
                    )
                }
                DropdownMenu(
                    expanded = dropdownExpand,
                    onDismissRequest = { dropdownExpand = false }
                ) {
                    DropdownMenuItem(onClick = {
                        onCopyClick()
                        dropdownExpand = false
                    }) {
                        Text(text = "复制")
                    }
                    DropdownMenuItem(onClick = {
                        onMoveClick()
                        dropdownExpand = false
                    }) {
                        Text(text = "移动")
                    }
                    DropdownMenuItem(onClick = {
                        onRenameClicked()
                        dropdownExpand = false
                    }) {
                        Text(text = "重命名")
                    }
                    DropdownMenuItem(onClick = {
                        onDeleteClicked()
                        dropdownExpand = false
                    }) {
                        Text(text = "删除")
                    }
                }
            }
        }
    }
}

@Composable
internal fun LegacyLevelItem(
    modifier: Modifier = Modifier,
    levelName: String,
    screenshot: String?,
    onRenameClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onCopyClick: () -> Unit,
    onMoveClick: () -> Unit
) {
    Card(modifier = modifier) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(
                            top = 16.dp,
                            start = 16.dp,
                            bottom = 16.dp
                        )
                        .weight(1f)
                ) {
                    Text(text = levelName, style = MaterialTheme.typography.h6)
                }
                LocalImage(
                    path = screenshot,
                    contentDescription = "存档预览图",
                    modifier = Modifier
                        .padding(16.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .aspectRatio(16f / 9f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    Modifier
                        .weight(1f)
                ) {
                    TextButton(onClick = onCopyClick) {
                        Text("复制")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = onMoveClick) {
                        Text("移动")
                    }
                }
                TextButton(
                    onClick = onRenameClicked
                ) { Text("重命名") }
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(
                    onClick = onDeleteClicked
                ) { Text("删除") }
            }
        }
    }
}

@Preview
@Composable
private fun LevelItemPreview() {
    HorizonManagerTheme {
        Surface(modifier = Modifier.wrapContentHeight()) {
            LegacyLevelItem(
                levelName = "My world",
                screenshot = null,
                onRenameClicked = {},
                onDeleteClicked = {},
                onCopyClick = {},
                onMoveClick = {}
            )
        }
    }
}