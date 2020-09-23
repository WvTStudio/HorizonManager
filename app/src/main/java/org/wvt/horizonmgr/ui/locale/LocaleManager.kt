package org.wvt.horizonmgr.ui.locale

import androidx.activity.ComponentActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animate
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.ui.HorizonManagerAmbient
import org.wvt.horizonmgr.ui.components.HorizonDivider
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import org.wvt.horizonmgr.ui.fileselector.SelectFileActivity
import org.wvt.horizonmgr.ui.main.DrawerStateAmbient
import org.wvt.horizonmgr.ui.theme.HorizonManagerTheme
import java.io.File

private enum class Tabs(val label: String) {
    MOD("Mod"), IC_MAP("IC地图"), MC_MAP("MC地图"), IC_TEXTURE("IC材质"), MC_TEXTURE("MC材质")
}

@Composable
fun LocalManager(selectedPackageUUID: String?) {
    val context = ContextAmbient.current as ComponentActivity
    val drawerState = DrawerStateAmbient.current
    var select by remember { mutableStateOf(0) }
    val tabs = remember { Tabs.values() }
    val scope = rememberCoroutineScope()
    var progressDialogState by remember { mutableStateOf<ProgressDialogState?>(null) }

    val horizonMgr = HorizonManagerAmbient.current

    Column {
        CustomAppBar(
            tabs = tabs.map { it.label },
            selectedTabIndex = select,
            onTabSelected = { select = it },
            onNavClicked = { drawerState.open() }
        )
        Stack(Modifier.fillMaxSize()) {
            Crossfade(current = tabs[select]) {
                if (selectedPackageUUID == null) {
                    Stack(Modifier.fillMaxSize()) {
                        ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = "请选择分包后再操作", style = MaterialTheme.typography.h6
                            )
                        }
                    }
                } else {
                    when (it) {
                        Tabs.MOD -> ModTab()
                        Tabs.IC_MAP -> LevelTab(LevelTabType.IC)
                        Tabs.MC_MAP -> LevelTab(LevelTabType.MC)
                        Tabs.IC_TEXTURE -> ResTab()
                        Tabs.MC_TEXTURE -> ResTab()
                    }
                }
            }
            if (selectedPackageUUID != null) ExtendedFloatingActionButton(
                modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd),
                icon = { Icon(Icons.Filled.Add) },
                text = { Text("安装") },
                onClick = {
                    // TODO 解耦
                    scope.launch {
                        val path = SelectFileActivity.startForResult(context)
                        if (selectedPackageUUID == null || path == null) return@launch
                        try {
                            progressDialogState = ProgressDialogState.Loading("正在安装")
                            if (horizonMgr.getFileType(File(path)) != HorizonManager.FileType.Mod) error(
                                "不是Mod"
                            )
                            horizonMgr.installMod(selectedPackageUUID, File(path))
                        } catch (e: Exception) {
                            progressDialogState =
                                ProgressDialogState.Failed("安装失败", "请检查您选择的文件格式是否正确")
                            return@launch
                        }
                        progressDialogState = ProgressDialogState.Finished("安装完成")
                    }
                }
            )
            progressDialogState?.let {
                ProgressDialog(
                    onCloseRequest = { progressDialogState = null },
                    state = it
                )
            }
        }
    }
}

@Composable
private fun CustomAppBar(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (index: Int) -> Unit,
    onNavClicked: () -> Unit
) {
    TopAppBar(title = {
        Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            HorizonDivider(Modifier.height(32.dp))
            ScrollableTabRow(
                modifier = Modifier.fillMaxHeight(),
                selectedTabIndex = selectedTabIndex,
                edgePadding = 0.dp,
                backgroundColor = Color.Transparent,
                indicator = {},
                divider = {}
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = selectedTabIndex == index
                    Column(
                        modifier = Modifier.selectable(
                            selected = selected,
                            onClick = { onTabSelected(index) },
                            indication = null
                        ).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = tabs[index],
                            color = animate(
                                if (selected) MaterialTheme.colors.onSurface
                                else MaterialTheme.colors.onSurface.copy(0.44f)
                            )
                        )
                    }
                }
            }
        }
    }, navigationIcon = {
        IconButton(onClick = onNavClicked, icon = {
            Icon(Icons.Filled.Menu)
        })
    }, backgroundColor = MaterialTheme.colors.surface)
}

@Preview
@Composable
private fun CustomAppBarPreview() {
    var selected by remember { mutableStateOf(0) }
    HorizonManagerTheme {
        Surface {
            CustomAppBar(
                tabs = Tabs.values().map { it.label },
                selectedTabIndex = selected,
                onTabSelected = { selected = it },
                onNavClicked = {}
            )
        }
    }
}

