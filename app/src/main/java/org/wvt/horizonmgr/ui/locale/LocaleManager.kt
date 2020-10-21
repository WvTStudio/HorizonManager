package org.wvt.horizonmgr.ui.locale

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.viewinterop.viewModel
import androidx.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.ui.components.HorizonDivider
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import org.wvt.horizonmgr.ui.main.SelectedPackageUUIDAmbient
import org.wvt.horizonmgr.ui.theme.HorizonManagerTheme
import java.io.File

enum class Tabs(val label: String) {
    MOD("Mod"), IC_MAP("IC地图"), MC_MAP("MC地图"), IC_TEXTURE("IC材质"), MC_TEXTURE("MC材质")
}

@Composable
fun LocalManager(
    horizonMgr: HorizonManager,
    onNavClicked: () -> Unit,
    requestSelectFile: suspend () -> String?
) {
    val vm = viewModel<LocaleManagerViewModel>()
    val selectedTab by vm.selectedTab.collectAsState()
    
    val scope = rememberCoroutineScope()
    
    var progressDialogState by remember { mutableStateOf<ProgressDialogState?>(null) }
    val selectedPackageUUID = SelectedPackageUUIDAmbient.current

    Column {
        CustomAppBar(
            tabs = vm.tabs,
            selectedTab = selectedTab,
            onTabSelected = { vm.selectedTab.value = it },
            onNavClicked = onNavClicked
        )
        Box(Modifier.fillMaxSize()) {
            Crossfade(current = selectedTab) {
                if (selectedPackageUUID == null) {
                    Box(Modifier.fillMaxSize()) {
                        ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.medium) {
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
            ExtendedFloatingActionButton(
                modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd),
                icon = { Icon(Icons.Filled.Add) },
                text = { Text("安装") },
                onClick = {
                    // TODO 解耦
                    scope.launch {
                        val path = requestSelectFile() ?: return@launch
                        try {
                            progressDialogState = ProgressDialogState.Loading("正在安装")
                            if (horizonMgr.getFileType(File(path)) != HorizonManager.FileType.Mod)
                                error("不是Mod")
                            horizonMgr.installMod(selectedPackageUUID!!, File(path))
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
    tabs: List<Tabs>,
    selectedTab: Tabs,
    onTabSelected: (index: Tabs) -> Unit,
    onNavClicked: () -> Unit
) {
    TopAppBar(title = {
        Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            HorizonDivider(Modifier.height(32.dp))
            ScrollableTabRow(
                modifier = Modifier.fillMaxHeight(),
                selectedTabIndex = tabs.indexOf(selectedTab),
                edgePadding = 0.dp,
                backgroundColor = Color.Transparent,
                indicator = {},
                divider = {}
            ) {
                tabs.fastForEachIndexed { index, tab ->
                    val selected = tab == selectedTab
                    Column(
                        modifier = Modifier.selectable(
                            selected = selected,
                            onClick = { onTabSelected(tab) },
                            indication = null
                        ).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = tab.label,
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
