package org.wvt.horizonmgr.ui.onlinemods

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
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
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.main.SelectedPackageUUIDAmbient

@Composable
fun Online(
    enable: Boolean,
    onNavClicked: () -> Unit
) {
    val vm = dependenciesViewModel<OnlineViewModel>()
    val state by vm.state.collectAsState()
    val options by vm.options.collectAsState()
    val downloadState by vm.downloadState.collectAsState()
    val installState by vm.installState.collectAsState()
    val selectedPackageUUID = SelectedPackageUUIDAmbient.current

    onCommit(enable) { vm.setEnable(enable) }

    onCommit(selectedPackageUUID) { vm.setSelectedUUID(selectedPackageUUID) }

    downloadState?.let { ProgressDialog(onCloseRequest = { vm.downloadFinish() }, state = it) }
    installState?.let { ProgressDialog(onCloseRequest = { vm.installFinish() }, state = it) }

    Column(Modifier.fillMaxSize()) {
        // Top App Bar
        TuneAppBar2(
            onNavClicked = onNavClicked,
            onFilterValueConfirm = { vm.setFilterValue(it) },
            vm.sources,
            options.selectedSource,
            vm::setSelectedSource,
            vm.sortModes,
            options.selectedSortMode,
            vm::setSelectedSortMode
        )
        // Content
        if (!enable) {
            NotLoginTip()
        } else {
            // If dit login, displays online mods list.
            Crossfade(
                modifier = Modifier.fillMaxSize(),
                current = state
            ) { state ->
                when (state) {
                    is OnlineViewModel.State.Loading -> Box(
                        Modifier.fillMaxSize(),
                        Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    is OnlineViewModel.State.OK -> Box(Modifier.fillMaxSize()) {
                        val lazyListState = rememberLazyListState()
                        LazyColumnFor(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 64.dp),
                            items = state.modList,
                            state = lazyListState
                        ) { item ->
                            ModItem(
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                                title = item.name,
                                text = item.description,
                                imageUrl = item.iconUrl,
                                onDownloadClick = { vm.download(item) },
                                onInstallClick = { vm.install(item) }
                            )
                        }
                    }
                    is OnlineViewModel.State.Error -> Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = vm::refresh) { Text("重试") }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotLoginTip() {
    Box(Modifier.fillMaxSize()) {
        Providers(AmbientContentAlpha provides ContentAlpha.medium) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "此功能仅在登录后可用",
                style = MaterialTheme.typography.h6
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
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
                Column(Modifier.weight(1f).padding(end = 8.dp)) {
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
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(4.dp))
                )
            }
            // Actions
            Row(
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Providers(AmbientContentColor provides MaterialTheme.colors.primary) {
                    // Install Button
                    IconButton(onClick = onInstallClick) {
                        Icon(Icons.Filled.Extension)
                    }
                    // Download Button
                    IconButton(
                        onClick = onDownloadClick,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.Filled.GetApp)
                    }
                }
            }
        }
    }
}