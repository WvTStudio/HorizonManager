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
import org.wvt.horizonmgr.ui.components.*

@Composable
fun ICResTab(
    viewModel: ICResTabViewModel,
    onAddButtonClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val packs by viewModel.resPacks.collectAsState()
    val errors by viewModel.errors.collectAsState()

    val progressState by viewModel.progressState.collectAsState()

    DisposableEffect(Unit) {
        viewModel.load()
        onDispose { }
    }

    val banner = @Composable {
        ErrorBanner(
            modifier = Modifier.fillMaxWidth(),
            errors = errors,
            text = "解析资源包时发生 ${errors.size} 个错误"
        )
    }

    Box(Modifier.fillMaxSize()) {
        Crossfade(targetState = state) { state ->
            when (state) {
                ICResTabViewModel.State.Done -> {
                    if (packs.isEmpty()) Box(Modifier.fillMaxWidth()) {
                        EmptyPage(Modifier.fillMaxSize()) {
                            Text("没有找到资源包")
                        }
                        banner()
                    } else LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { banner() }
                        item { Spacer(Modifier.height(8.dp))}
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
                        item { Spacer(Modifier.height(24.dp))}
                    }
                }
                ICResTabViewModel.State.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                is ICResTabViewModel.State.Error -> ErrorPage(
                    modifier = Modifier.fillMaxSize(),
                    message = { Text(text = state.message) },
                    onRetryClick = { viewModel.load() }
                )
            }
        }
        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            onClick = onAddButtonClick
        ) { Icon(Icons.Default.Add, "Add") }

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