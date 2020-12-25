package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animate
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.gesture.longPressGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.components.EmptyPage
import org.wvt.horizonmgr.ui.components.LocalImage
import org.wvt.horizonmgr.ui.components.ProgressDialog

@Composable
internal fun ModTab(
    vm: ModTabViewModel
) {
    val ps by vm.progressState.collectAsState()
    val state by vm.state.collectAsState()
    val mods by vm.mods.collectAsState()
    val enabledMods by vm.enabledMods.collectAsState()

    Crossfade(current = state) { state ->
        when (state) {
            is ModTabViewModel.State.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
            is ModTabViewModel.State.PackageNotSelected -> {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "你还没有选择分包", color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium))
                }
            }
            is ModTabViewModel.State.OK -> if (mods.isEmpty()) {
                EmptyPage(Modifier.fillMaxSize()) {
                    Text("当前分包内没有已安装的模组", color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 8.dp, bottom = 64.dp)
                ) {
                    itemsIndexed(items = mods) { index, item ->
                        ModItem(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            enable = enabledMods.contains(item.id),
                            title = item.name,
                            text = item.description,
                            iconPath = item.iconPath,
                            selected = false,
                            onLongClick = {},
                            onEnabledChange = { if (it) vm.enableMod(item) else vm.disableMod(item) },
                            onClick = {/* TODO 显示 Mod 详情 */ },
                            onDeleteClick = { vm.deleteMod(item) }
                        )
                    }
                }
            }
        }

    }
    ps?.let {
        ProgressDialog(onCloseRequest = vm::dismiss, state = it)
    }
}

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
    val interactionState = remember { InteractionState() }
    Card(
        modifier = modifier.clickable(
            onClick = onClick,
            interactionState = interactionState,
            indication = null
        ).longPressGestureFilter { onLongClick() },
        border = if (selected) BorderStroke(
            1.dp, MaterialTheme.colors.primary
        ) else null,
        elevation = 2.dp
    ) {
        Column(
            Modifier.indication(interactionState, indication = rememberRipple())
                .background(animate(if (selected) MaterialTheme.colors.primary.copy(0.12f) else Color.Transparent))
                .padding(top = 16.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
        ) {
            Row {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.h5)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text, style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
                // Mod Icon
                LocalImage(
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(4.dp)),
                    path = iconPath,
                )
            }
            // Footer controller buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp, end = 8.dp),
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