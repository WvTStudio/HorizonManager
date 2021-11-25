package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.components.*
import org.wvt.horizonmgr.viewmodel.ModTabViewModel

@Composable
internal fun ModTab(
    vm: ModTabViewModel,
    onAddModClicked: () -> Unit
) {
    val ps by vm.progressState.collectAsState()
    val state by vm.state.collectAsState()
    val mods by vm.mods.collectAsState()
    val enabledMods by vm.newEnabledMods.collectAsState()
    val errors by vm.errors.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()

    val banner = @Composable {
        ErrorBanner(
            modifier = Modifier.fillMaxWidth(),
            errors = errors,
            text = stringResource(R.string.ic_mod_tab_banner).format(errors.size)
        )
    }

    LaunchedEffect(Unit) { vm.refresh() }

    Crossfade(state) { state ->
        when (state) {
            is ModTabViewModel.State.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
            is ModTabViewModel.State.PackageNotSelected -> {
                EmptyPage(Modifier.fillMaxSize()) {
                    Text(text = stringResource(R.string.ic_mod_tab_unselected))
                }
            }
            is ModTabViewModel.State.OK -> Box(Modifier.fillMaxSize()) {
                SwipeRefresh(
                    modifier = Modifier.fillMaxSize(),
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = vm::refresh,
                    indicator = { state, distance ->
                        SwipeRefreshIndicator(
                            state = state,
                            refreshTriggerDistance = distance,
                            contentColor = MaterialTheme.colors.primary
                        )
                    }
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (mods.isEmpty()) item {
                            Box(Modifier.fillParentMaxSize()) {
                                EmptyPage(Modifier.fillMaxSize()) {
                                    Text(stringResource(R.string.ic_mod_tab_tip_empty))
                                }
                                banner()
                            }
                        } else {
                            item { banner() }
                            item { Spacer(Modifier.height(8.dp)) }
                            itemsIndexed(items = mods) { _, item ->
                                ModItem(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    enable = enabledMods.contains(item),
                                    title = item.name,
                                    text = item.description,
                                    iconPath = item.iconPath,
                                    selected = false,
                                    onLongClick = {},
                                    onEnabledChange = {
                                        if (it) vm.enableMod(item)
                                        else vm.disableMod(item)
                                    },
                                    onClick = {/* TODO 显示 Mod 详情 */ },
                                    onDeleteClick = { vm.deleteMod(item) }
                                )
                            }
                            item { Spacer(Modifier.height(64.dp)) }
                        }
                    }
                }
                FloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    onClick = onAddModClicked
                ) { Icon(Icons.Rounded.Add, stringResource(R.string.ic_mod_tab_action_install)) }
            }
            is ModTabViewModel.State.Error -> ErrorPage(
                message = { Text(stringResource(R.string.ic_mod_tab_tip_error)) },
                onRetryClick = { vm.refresh() }
            )
        }
        ps?.let {
            ProgressDialog(onCloseRequest = vm::dismiss, state = it)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModItem(
    modifier: Modifier = Modifier,
    enable: Boolean,
    title: String,
    text: String,
    iconPath: String?,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEnabledChange: (enable: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val icon = iconPath?.let { loadLocalImage(path = iconPath) }

    Card(
        modifier = modifier,
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colors.primary) else null,
        elevation = animateDpAsState(targetValue = if (isPressed) 8.dp else 1.dp).value
    ) {
        Column(
            Modifier
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick, onLongClick = onLongClick
                )
                .background(
                    animateColorAsState(
                        if (selected) MaterialTheme.colors.primary.copy(0.12f)
                        else Color.Transparent
                    ).value
                )
                .padding(top = 16.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
        ) {
            Row {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.h5)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.subtitle1,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Mod Icon
                ModIcon(
                    modifier = Modifier.size(80.dp),
                    image = icon?.value
                )
            }
            // Footer controller buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button
                Box(Modifier.weight(1f)) {
                    TextButton(onClick = onDeleteClick) { Text(stringResource(R.string.ic_mod_tab_action_delete)) }
                }
                // Enable switcher
                Switch(checked = enable, onCheckedChange = onEnabledChange)
            }
        }
    }
}