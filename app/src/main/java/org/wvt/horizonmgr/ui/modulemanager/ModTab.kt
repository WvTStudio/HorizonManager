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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.components.*

@Composable
internal fun ModTab(
    vm: ModTabViewModel,
    onAddModClicked: () -> Unit
) {
    val ps by vm.progressState.collectAsState()
    val state by vm.state.collectAsState()
    val mods by vm.mods.collectAsState()
    val enabledMods by vm.newEnabledMods.collectAsState()

    Crossfade(state) { state ->
        when (state) {
            is ModTabViewModel.State.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
            is ModTabViewModel.State.PackageNotSelected -> {
                EmptyPage(Modifier.fillMaxSize()) {
                    Text(text = "你还没有选择分包")
                }
            }
            is ModTabViewModel.State.OK -> Box(Modifier.fillMaxSize()) {
                if (mods.isEmpty()) {
                    EmptyPage(Modifier.fillMaxSize()) {
                        Text("当前分包内没有已安装的模组")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 8.dp, bottom = 64.dp)
                    ) {
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
                    }
                }
                ExtendedFloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                    text = { Text("安装") },
                    onClick = onAddModClicked
                )
            }
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
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(4.dp)),
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
                    TextButton(onClick = onDeleteClick) { Text("删除") }
                }
                // Enable switcher
                Switch(checked = enable, onCheckedChange = onEnabledChange)
            }
        }
    }
}