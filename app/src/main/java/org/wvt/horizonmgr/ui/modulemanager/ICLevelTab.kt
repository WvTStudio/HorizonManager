package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.ui.components.*

@Composable
internal fun ICLevelTab(
    viewModel: ICLevelTabViewModel,
    onAddButtonClicked: () -> Unit
) {
    val levels by viewModel.levels.collectAsState()
    val state by viewModel.state.collectAsState()
    val progressState by viewModel.progressState.collectAsState()
    val inputDialogState = viewModel.inputDialogState

    DisposableEffect(Unit) {
        viewModel.load()
        onDispose { }
    }

    Crossfade(state) {
        when (it) {
            ICLevelTabViewModel.State.Loading -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
            ICLevelTabViewModel.State.PackageNotSelected -> {
                EmptyPage(Modifier.fillMaxSize()) {
                    Text("你还没有选择分包")
                }
            }
            ICLevelTabViewModel.State.OK -> Box(Modifier.fillMaxSize()) {
                if (levels.isEmpty()) {
                    EmptyPage(Modifier.fillMaxSize()) {
                        Text("当前还没有地图")
                    }
                } else LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 64.dp)
                ) {
                    itemsIndexed(items = levels) { _, item ->
                        LevelItem(
                            modifier = Modifier.padding(16.dp),
                            levelName = item.name,
                            screenshot = item.screenshot,
                            onRenameClicked = { viewModel.renameLevel(item) },
                            onDeleteClicked = { viewModel.deleteLevel(item) },
                            onCopyClick = { viewModel.copy(item) },
                            onMoveClick = { viewModel.move(item) }
                        )
                    }
                }
                FloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    onClick = onAddButtonClicked
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
            is ICLevelTabViewModel.State.Error -> ErrorPage(
                modifier = Modifier.fillMaxSize(),
                message = { Text("出现错误") },
                onRetryClick = { viewModel.load() }
            )
        }
    }


    InputDialogHost(state = inputDialogState)

    progressState?.let {
        ProgressDialog(onCloseRequest = { viewModel.dismissProgressDialog() }, state = it)
    }
}