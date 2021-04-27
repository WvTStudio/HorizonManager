package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.wvt.horizonmgr.ui.components.*

@Composable
fun MCResTab(
    viewModel: MCResTabViewModel,
    onAddButtonClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val packs by viewModel.resPacks.collectAsState()
    val errors by viewModel.errors.collectAsState()
    val progressState by viewModel.progressState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    val banner = @Composable {
        ErrorBanner(
            modifier = Modifier.fillMaxWidth(),
            errors = errors,
            text = "解析资源包时出现 ${errors.size} 个错误"
        )
    }

    Box(Modifier.fillMaxSize()) {
        Crossfade(targetState = state) {
            when (it) {
                MCResTabViewModel.State.Done -> Box(Modifier.fillMaxSize()) {
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
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            if (packs.isEmpty()) item {
                                Box(Modifier.fillParentMaxSize()) {
                                    EmptyPage(Modifier.fillMaxSize()) {
                                        Text("没有找到资源包")
                                    }
                                    banner()
                                }
                            } else {
                                item { banner() }
                                item { Spacer(Modifier.height(8.dp)) }
                                itemsIndexed(packs) { index, item ->
                                    ResItem(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .fillParentMaxWidth(),
                                        icon = item.iconPath,
                                        name = item.manifest.header.name,
                                        description = item.manifest.header.description,
                                        onClick = {}
                                    )
                                }
                                item { Spacer(Modifier.height(24.dp)) }
                            }
                        }
                    }
                    FloatingActionButton(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomEnd),
                        onClick = onAddButtonClick
                    ) { Icon(Icons.Default.Add, "Add") }
                }
                MCResTabViewModel.State.Loading -> Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        Modifier.align(Alignment.Center)
                    )
                }
                is MCResTabViewModel.State.Error -> ErrorPage(
                    modifier = Modifier.fillMaxSize(),
                    message = { Text(text = it.message) },
                    onRetryClick = { viewModel.refresh() }
                )
            }
        }
        progressState?.let {
            ProgressDialog(onCloseRequest = { viewModel.dismissProgressDialog() }, state = it)
        }
    }
}

@Composable
private fun ResItem(
    modifier: Modifier,
    icon: String?,
    name: String,
    description: String,
    onClick: () -> Unit
) {
    val iconImage = icon?.let { loadLocalImage(path = it) }

    Card(modifier) {
        Box(Modifier.clickable(onClick = onClick)) {
            Row(Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    Text(text = name, style = MaterialTheme.typography.h6)
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(text = description, style = MaterialTheme.typography.body1)
                    }
                }
                Box(Modifier.padding(16.dp)) {
                    ModIcon(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        image = iconImage?.value
                    )
                }
            }
        }
    }
}