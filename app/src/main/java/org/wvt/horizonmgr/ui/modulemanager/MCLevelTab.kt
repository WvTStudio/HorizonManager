package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.components.*

@Composable
internal fun MCLevelTab(
    viewModel: MCLevelTabViewModel,
    onAddClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val errors by viewModel.errors.collectAsState()
    val items by viewModel.levels.collectAsState()
    val progressState by viewModel.progressState.collectAsState()
    val inputDialogState = viewModel.inputDialogState

    DisposableEffect(Unit) {
        viewModel.load()
        onDispose { }
    }

    val banner = @Composable {
        ErrorBanner(
            modifier = Modifier.fillMaxWidth(),
            errors = errors,
            text = "解析地图时发生 ${errors.size} 个错误"
        )
    }

    Crossfade(targetState = state) {
        when (it) {
            MCLevelTabViewModel.State.Done -> Box(Modifier.fillMaxSize()) {
                if (items.isEmpty()) {
                    Box(Modifier.fillMaxSize()) {
                        EmptyPage(Modifier.fillMaxSize()) {
                            Text("当前还没有地图")
                        }
                        banner()
                    }
                } else LazyColumn(contentPadding = PaddingValues(bottom = 64.dp)) {
                    item { banner() }
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
            is MCLevelTabViewModel.State.Error -> ErrorPage(
                modifier = Modifier.fillMaxSize(),
                message = { Text(it.message) },
                onRetryClick = { viewModel.load() }
            )
            MCLevelTabViewModel.State.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    InputDialogHost(state = inputDialogState)

    progressState?.let {
        ProgressDialog(onCloseRequest = { viewModel.dismissProgressDialog() }, state = it)
    }
}