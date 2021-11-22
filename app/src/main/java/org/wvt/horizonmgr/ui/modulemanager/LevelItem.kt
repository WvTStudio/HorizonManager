package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R
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
                path = screenshot,
                contentDescription = stringResource(R.string.module_screen_level_item_image_desc),
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
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(R.string.module_screen_level_item_options),
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
                        Text(text = stringResource(R.string.module_screen_level_item_action_copy))
                    }
                    DropdownMenuItem(onClick = {
                        onMoveClick()
                        dropdownExpand = false
                    }) {
                        Text(text = stringResource(R.string.module_screen_level_item_action_move))
                    }
                    DropdownMenuItem(onClick = {
                        onRenameClicked()
                        dropdownExpand = false
                    }) {
                        Text(text = stringResource(R.string.module_screen_level_item_action_rename))
                    }
                    DropdownMenuItem(onClick = {
                        onDeleteClicked()
                        dropdownExpand = false
                    }) {
                        Text(text = stringResource(R.string.module_screen_level_item_action_delete))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LevelItemPreview() {
    HorizonManagerTheme {
        Surface(modifier = Modifier.wrapContentHeight()) {
            LevelItem(
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