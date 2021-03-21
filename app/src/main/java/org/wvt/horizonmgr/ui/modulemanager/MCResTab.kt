package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
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
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.components.ModIcon
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.components.loadLocalImage

@Composable
fun MCResTab(
    viewModel: MCResTabViewModel,
    onAddButtonClick: () -> Unit
) {
    val packs by viewModel.resPacks.collectAsState()
    val state by viewModel.state.collectAsState()
    val progressState by viewModel.progressState.collectAsState()

    DisposableEffect(Unit) {
        viewModel.load()
        onDispose { }
    }
    Box(Modifier.fillMaxSize()) {
        Crossfade(targetState = state) {
            Box(Modifier.fillMaxSize()) {
                when (it) {
                    MCResTabViewModel.State.FINISHED -> ResList(
                        modifier = Modifier.fillMaxSize(),
                        data = packs
                    ) { _, item ->
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
                    MCResTabViewModel.State.LOADING -> CircularProgressIndicator(
                        Modifier.align(Alignment.Center)
                    )
                    MCResTabViewModel.State.FAILED -> ErrorPage(
                        modifier = Modifier.fillMaxSize(),
                        message = { Text(text = "出现错误") },
                        onRetryClick = { viewModel.load() }
                    )
                }
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
fun <T> ResList(
    modifier: Modifier,
    data: List<T>,
    item: @Composable LazyItemScope.(index: Int, item: T) -> Unit
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
        itemsIndexed(data) { index, item ->
            item(index, item)
        }
    }
}

@Composable
fun ResItem(
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