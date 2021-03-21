package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.components.*

@Composable
internal fun MCLevelTab(
    viewModel: MCLevelTabViewModel,
    onAddClick: () -> Unit
) {
    val items by viewModel.levels.collectAsState()
    val progressState by viewModel.progressState.collectAsState()
    val inputDialogState = viewModel.inputDialogState

    DisposableEffect(Unit) {
        viewModel.load()
        onDispose { }
    }

    Box(Modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            EmptyPage(Modifier.matchParentSize()) {
                Text("当前还没有地图")
            }
        } else LazyColumn(contentPadding = PaddingValues(bottom = 64.dp)) {
            itemsIndexed(items = items) { _, item ->
                LevelItem(
                    modifier = Modifier.padding(16.dp),
                    levelName = item.name,
                    screenshot = item.screenshot,
                    onRenameClicked = { viewModel.rename(item) },
                    onDeleteClicked = { viewModel.delete(item) },
                    onMoveClick = { viewModel.move(item) },
                    onCopyClick = { viewModel.copy(item) }
                )
            }
        }
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp), onClick = onAddClick
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
        }
    }

    InputDialogHost(state = inputDialogState)

    progressState?.let {
        ProgressDialog(onCloseRequest = { viewModel.dismissProgressDialog() }, state = it)
    }
}