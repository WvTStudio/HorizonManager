package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.wvt.horizonmgr.ui.components.*
import org.wvt.horizonmgr.viewmodel.ICLevelTabViewModel

@Composable
internal fun ICLevelTab(
    viewModel: ICLevelTabViewModel,
    onAddButtonClicked: () -> Unit
) {
    val levels by viewModel.levels.collectAsState()
    val state by viewModel.state.collectAsState()
    val progressState by viewModel.progressState.collectAsState()
    val inputDialogState = viewModel.inputDialogState
    val errors by viewModel.errors.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val banner = @Composable {
        ErrorBanner(
            modifier = Modifier.fillMaxWidth(),
            errors = errors,
            text = "解析地图时发生 ${errors.size} 个错误"
        )
    }

    LaunchedEffect(Unit) { viewModel.refresh() }

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
                SwipeRefresh(
                    modifier = Modifier.fillMaxSize(),
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = viewModel::refresh,
                    indicator = { state, distance ->
                        SwipeRefreshIndicator(
                            state = state,
                            refreshTriggerDistance = distance,
                            contentColor = MaterialTheme.colors.primary
                        )
                    }
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 64.dp)
                    ) {
                        if (levels.isEmpty()) item {
                            Box(Modifier.fillParentMaxSize()) {
                                EmptyPage(Modifier.fillMaxSize()) {
                                    Text("当前还没有地图")
                                }
                                banner()
                            }
                        } else {
                            item {
                                banner()
                            }
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
                    }
                }
                FloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    onClick = onAddButtonClicked
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add")
                }
            }
            is ICLevelTabViewModel.State.Error -> ErrorPage(
                modifier = Modifier.fillMaxSize(),
                message = { Text(it.message) },
                onRetryClick = { viewModel.refresh() }
            )
        }
    }


    InputDialogHost(state = inputDialogState)

    progressState?.let {
        ProgressDialog(onCloseRequest = { viewModel.dismissProgressDialog() }, state = it)
    }
}