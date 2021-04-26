package org.wvt.horizonmgr.ui.downloaded

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.wvt.horizonmgr.ui.components.ModIcon
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor

@Composable
fun DownloadedMods(
    vm: DMViewModel,
    onNavClicked: () -> Unit
) {
    val mods by vm.mods.collectAsState()
    val progressState by vm.progressState.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()

    LaunchedEffect(vm) { vm.init() }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier.zIndex(4.dp.value), title = {
                Text("本地资源")
            }, navigationIcon = {
                IconButton(onClick = onNavClicked, content = {
                    Icon(Icons.Filled.Menu, "菜单")
                })
            }, backgroundColor = AppBarBackgroundColor
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            SwipeRefresh(
                modifier = Modifier.fillMaxSize(),
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { vm.refresh() },
                indicator = { state, distance ->
                    SwipeRefreshIndicator(
                        state = state,
                        refreshTriggerDistance = distance,
                        contentColor = MaterialTheme.colors.primary
                    )
                }
            ) {
                ModList(mods = mods) { _, item ->
                    ModItem(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        name = item.name,
                        description = item.description,
                        icon = {
                            Box(Modifier.padding(16.dp)) {
                                // Mod Icon
                                ModIcon(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    image = item.icon
                                )
                            }
                        },
                        onInstallClicked = { vm.install(item) },
                        onDeleteClicked = { vm.delete(item) }
                    )
                }
            }
        }
    }

    progressState?.let {
        ProgressDialog(onCloseRequest = vm::dismiss, state = it)
    }
}

@Composable
private fun ModList(
    mods: List<DMViewModel.DownloadedMod>,
    item: @Composable LazyItemScope.(index: Int, item: DMViewModel.DownloadedMod) -> Unit
) {
    Crossfade(mods) { mods ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 64.dp)
        ) {
            if (mods.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillParentMaxSize()
                            .padding(16.dp), Alignment.Center
                    ) {
                        Text(
                            "未找到本地资源，请从在线资源中下载",
                            color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)
                        )
                    }
                }
            }
            itemsIndexed(mods) { index, item ->
                item(index, item)
            }
        }
    }
}

@Composable
private fun ModItem(
    modifier: Modifier = Modifier,
    name: String,
    description: String,
    icon: @Composable () -> Unit,
    onDeleteClicked: () -> Unit,
    onInstallClicked: () -> Unit,
) {
    Card(modifier = modifier) {
        Column {
            Row {
                Column(
                    Modifier
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                        .weight(1f)
                ) {
                    // Name
                    Text(text = name, style = MaterialTheme.typography.h6)
                    // Description
                    Text(
                        modifier = Modifier.padding(top = 16.dp),
                        text = description
                    )
                }
                icon()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                // Delete Button
                TextButton(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterStart),
                    onClick = onDeleteClicked
                ) { Text("删除", color = MaterialTheme.colors.primary) }
                // Install Button
                IconButton(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .align(Alignment.CenterEnd),
                    onClick = onInstallClicked
                ) {
                    Icon(
                        imageVector = Icons.Filled.Extension,
                        tint = MaterialTheme.colors.primary,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

private sealed class DownloadItemState {
    object Finished : DownloadItemState()
    class Error(val message: Exception) : DownloadItemState()
    class Downloading(
        val name: String,
        val progress: Float
    ) : DownloadItemState()
}