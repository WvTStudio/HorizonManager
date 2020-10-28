package org.wvt.horizonmgr.ui.locale

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animate
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.RippleIndication
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.gesture.longPressGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.components.LocalImage
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.main.SelectedPackageUUIDAmbient

@Composable
internal fun ModTab() {
    val vm = dependenciesViewModel<ModTabViewModel>()
    val ps by vm.progressState.collectAsState()
    val state by vm.state.collectAsState()
    val mods by vm.mods.collectAsState()
    val enabledMods by vm.enabledMods.collectAsState()
    val selectedUUID = SelectedPackageUUIDAmbient.current

    onCommit(selectedUUID) {
        vm.setSelectedUUID(selectedUUID)
        vm.load()
    }

    Crossfade(current = state) { state ->
        when (state) {
            is ModTabViewModel.State.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
            is ModTabViewModel.State.PackageNotSelected -> {
                Box(Modifier.fillMaxSize()) {
                    Text(modifier = Modifier.align(Alignment.Center), text = "请先选择分包")
                }
            }
            is ModTabViewModel.State.OK -> LazyColumnForIndexed(
                items = mods,
                contentPadding = PaddingValues(top = 8.dp, bottom = 64.dp)
            ) { index, item ->
                ModItem(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    enable = remember(enabledMods) { enabledMods.contains(item.id) },
                    title = item.name,
                    text = item.description,
                    iconPath = item.iconPath,
                    selected = false,
                    onLongClick = {},
                    onEnabledChange = { if (it) vm.enableMod(item) else vm.disableMod(item) },
                    onClick = {/* TODO */ },
                    onDeleteClick = { vm.deleteMod(item) }
                )
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
            Modifier.indication(interactionState, indication = RippleIndication())
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
                LocalImage(
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(4.dp)),
                    path = iconPath,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    TextButton(onClick = onDeleteClick) { Text("删除") }
                }
                Switch(checked = enable, onCheckedChange = onEnabledChange)
            }
        }
    }
}