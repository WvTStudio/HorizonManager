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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.components.*
import org.wvt.horizonmgr.viewmodel.MCLevelTabViewModel

@Composable
internal fun MCLevelTab(
    viewModel: MCLevelTabViewModel,
    onAddClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val errors by viewModel.errors.collectAsState()
    val items by viewModel.levels.collectAsState()
    val progressState by viewModel.progressState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val inputDialogState = viewModel.inputDialogState

    LaunchedEffect(Unit) { viewModel.refresh() }

    val banner = @Composable {
        ErrorBanner(
            modifier = Modifier.fillMaxWidth(),
            errors = errors,
            text = stringResource(R.string.md_level_tab_banner).format(errors.size)
        )
    }

    Crossfade(targetState = state) {
        when (it) {
            MCLevelTabViewModel.State.Done -> Box(Modifier.fillMaxSize()) {
                SwipeRefresh(
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
                        if (items.isEmpty()) item {
                            Box(Modifier.fillParentMaxSize()) {
                                EmptyPage(Modifier.fillMaxSize()) {
                                    Text(stringResource(R.string.mc_level_tab_tip_empty))
                                }
                                banner()
                            }
                        } else {
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
                    }
                }
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp), onClick = onAddClick
                ) {
                    Icon(Icons.Rounded.Add, stringResource(R.string.mc_level_tab_action_add))
                }
            }
            is MCLevelTabViewModel.State.Error -> ErrorPage(
                modifier = Modifier.fillMaxSize(),
                message = { Text(it.message) },
                onRetryClick = { viewModel.refresh() }
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