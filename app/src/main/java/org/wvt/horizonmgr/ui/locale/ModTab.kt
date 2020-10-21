package org.wvt.horizonmgr.ui.locale

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.gesture.longPressGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.ui.HorizonManagerAmbient
import org.wvt.horizonmgr.ui.components.LocalImage
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import org.wvt.horizonmgr.ui.main.SelectedPackageUUIDAmbient
import org.wvt.horizonmgr.ui.theme.HorizonManagerTheme

@Composable
internal fun ModTab() {
    val selectedPackageUUID = SelectedPackageUUIDAmbient.current!!

    val items = remember {
        mutableStateListOf<HorizonManager.InstalledModInfo>()
    }

    val scope = rememberCoroutineScope()
    val horizonMgr = HorizonManagerAmbient.current
    var progressDialogState by remember { mutableStateOf<ProgressDialogState?>(null) }

    suspend fun load() {
        try {
            items.clear()
            items.addAll(horizonMgr.getMods(selectedPackageUUID))
            /*items = mutableStateListOf<HorizonManager.InstalledModInfo>().apply {
                addAll(horizonMgr.getMods(selectedPackageUUID))
            }*/
        } catch (e: HorizonManager.PackageNotFoundException) {
            // TODO
        } catch (e: Exception) {
            // TODO
        }
    }

    launchInComposition {
        load()
    }

    Crossfade(current = items) { items ->
        LazyColumnForIndexed(items = items) { index, item ->
            if (index == 0) Spacer(Modifier.height(8.dp))
            ModItem(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                enable = item.enable,
                title = item.name,
                text = item.description,
                iconPath = item.iconPath,
                onEnabledChange = {
                    scope.launch {
                        try {
                            if (it) {
                                horizonMgr.enableModByPath(item.path)
                                items.set(index, item.copy(enable = true))
                            } else {
                                horizonMgr.disableModByPath(item.path)
                                items.set(index, item.copy(enable = false))
                            }
                        } catch (e: Exception) {
                            // TODO 失败提示
                        }
                    }
                },
                onClick = {
                    // TODO
                },
                onDeleteClick = {
                    scope.launch {
                        progressDialogState = ProgressDialogState.Loading("正在删除")
                        try {
                            horizonMgr.deleteModByPath(item.path)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            progressDialogState =
                                ProgressDialogState.Failed("删除失败", e.localizedMessage)
                            return@launch
                        }
                        load()
                        progressDialogState = ProgressDialogState.Finished("删除成功")
                    }
                }
            )
            if (index == items.size - 1) Spacer(modifier = Modifier.size(64.dp))
        }
    }
    progressDialogState?.let {
        ProgressDialog(onCloseRequest = { progressDialogState = null }, state = it)
    }
}

@Composable
private fun ModItem(
    modifier: Modifier = Modifier,
    enable: Boolean,
    title: String,
    text: String,
    iconPath: String?,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEnabledChange: (enable: Boolean) -> Unit
) {
    var selected by remember { mutableStateOf(false) }

    Card(
        modifier.clickable(onClick = onClick)
            .longPressGestureFilter { selected = !selected },
        border = if (selected) BorderStroke(
            1.dp,
            MaterialTheme.colors.primary.copy(0.12f)
        ) else null,
        backgroundColor = animate(if (selected) Color(0xFFE0F7FA) else MaterialTheme.colors.surface),
        elevation = 2.dp
    ) {
        Column(
            Modifier.padding(top = 16.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
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
                Stack(modifier = Modifier.weight(1f)) {
                    TextButton(onClick = onDeleteClick) { Text("删除") }
                }
                Switch(checked = enable, onCheckedChange = onEnabledChange)
            }
        }
    }
}


@Preview
@Composable
private fun ModItemPreview() {
    HorizonManagerTheme {
        Surface {
            ModItem(
                enable = true,
                title = "Test Mod",
                text = "This is a test mod",
                iconPath = "",
                onClick = {},
                onEnabledChange = {},
                onDeleteClick = {}
            )
        }
    }
}
