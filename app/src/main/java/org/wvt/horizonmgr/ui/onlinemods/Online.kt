package org.wvt.horizonmgr.ui.onlinemods

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.legacyservice.WebAPI
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.main.LocalSelectedPackageUUID

@Composable
fun Online(
    viewModel: OnlineViewModel,
    enable: Boolean,
    onNavClicked: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val options by viewModel.options.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val installState by viewModel.installState.collectAsState()
    val selectedPackageUUID = LocalSelectedPackageUUID.current

    DisposableEffect(enable) {
        viewModel.setEnable(enable)
        onDispose { }
    }
    DisposableEffect(selectedPackageUUID) {
        viewModel.setSelectedUUID(selectedPackageUUID)
        onDispose { }
    }

    downloadState?.let { ProgressDialog(onCloseRequest = { viewModel.downloadFinish() }, state = it) }
    installState?.let { ProgressDialog(onCloseRequest = { viewModel.installFinish() }, state = it) }

    Column(Modifier.fillMaxSize()) {
        // Top App Bar
        TuneAppBar(
            enable = enable,
            onNavClicked = onNavClicked,
            onFilterValueConfirm = { viewModel.setFilterValue(it) },
            viewModel.sources,
            options.selectedSource,
            viewModel::setSelectedSource,
            viewModel.sortModes,
            options.selectedSortMode,
            viewModel::setSelectedSortMode
        )
        // Content
        if (!enable) {
            NotLoginTip()
        } else {
            // If do login, displays online mods list.
            Crossfade(
                modifier = Modifier.fillMaxSize(),
                targetState= state
            ) { state ->
                when (state) {
                    is OnlineViewModel.State.Loading -> Box(
                        Modifier.fillMaxSize(),
                        Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    is OnlineViewModel.State.OK -> ModList(
                        modifier = Modifier.fillMaxSize(),
                        mods = state.modList,
                        onDownloadClick = { viewModel.download(it) },
                        onInstallClick = { viewModel.install(it) }
                    )
                    is OnlineViewModel.State.Error -> ErrorPage(
                        modifier = Modifier.fillMaxSize(),
                        message = {
                            Text(state.message)
                        },
                        onRetryClick = viewModel::refresh
                    )
                }
            }
        }
    }
}

@Composable
private fun NotLoginTip() {
    Box(Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "此功能仅在登录后可用",
            color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)
        )
    }
}

@Composable
private fun ModList(
    modifier: Modifier = Modifier,
    mods: List<WebAPI.OnlineModInfo>,
    onDownloadClick: (WebAPI.OnlineModInfo) -> Unit,
    onInstallClick: (WebAPI.OnlineModInfo) -> Unit
) {
    Box(modifier) {
        val lazyListState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 64.dp),
            state = lazyListState
        ) {
            itemsIndexed(mods) { _, item ->
                ModItem(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    title = item.name,
                    text = item.description,
                    imageUrl = item.iconUrl,
                    onDownloadClick = { onDownloadClick(item) },
                    onInstallClick = { onInstallClick(item) }
                )
            }
        }
    }
}

@Composable
private fun ModItem(
    modifier: Modifier = Modifier,
    title: String, text: String, imageUrl: String,
    onDownloadClick: () -> Unit,
    onInstallClick: () -> Unit
) {
    Card(modifier = modifier, elevation = 1.dp) {
        Column(
            Modifier.padding(top = 16.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
        ) {
            // Information
            Row {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    // Title
                    Text(title, style = MaterialTheme.typography.h5)
                    // Secondary Text
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = text, style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
                // Image
                NetworkImage(
                    url = imageUrl,
                    contentDescription = "模组图标",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
            // Actions
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.primary) {
                    // Install Button
                    IconButton(onClick = onInstallClick) {
                        Icon(Icons.Filled.Extension, contentDescription = "安装")
                    }
                    // Download Button
                    IconButton(
                        onClick = onDownloadClick,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.Filled.GetApp, contentDescription = "仅下载")
                    }
                }
            }
        }
    }
}