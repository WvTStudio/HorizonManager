package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.zIndex
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.components.HorizonDivider
import org.wvt.horizonmgr.ui.components.ProgressDialog

@Composable
fun ModuleManager(
    onNavClicked: () -> Unit,
    requestSelectFile: suspend () -> String?
) {
    val vm = dependenciesViewModel<ModuleManagerViewModel>()
    val selectedTab by vm.selectedTab.collectAsState()
    val ps by vm.progressState.collectAsState()
    val selected by vm.selectedPackage.collectAsState()

    Column {
        CustomAppBar(
            tabs = vm.tabs,
            selectedTab = selectedTab,
            onTabSelected = vm::selectTab,
            onNavClicked = onNavClicked
        )
        Box(Modifier.fillMaxSize()) {
            Crossfade(current = selectedTab) {
                when (it) {
                    ModuleManagerViewModel.Tabs.MOD -> ModTab()
                    ModuleManagerViewModel.Tabs.IC_MAP -> LevelTab(LevelTabType.IC)
                    ModuleManagerViewModel.Tabs.MC_MAP -> LevelTab(LevelTabType.MC)
                    ModuleManagerViewModel.Tabs.IC_TEXTURE -> ResTab()
                    ModuleManagerViewModel.Tabs.MC_TEXTURE -> ResTab()
                }
            }
            ExtendedFloatingActionButton(
                modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd),
                icon = { Icon(Icons.Filled.Add) },
                text = { Text("安装") },
                onClick = { vm.install(requestSelectFile) }
            )
            ps?.let {
                ProgressDialog(onCloseRequest = vm::dismiss, state = it)
            }
        }
    }
}

@Composable
private fun CustomAppBar(
    tabs: List<ModuleManagerViewModel.Tabs>,
    selectedTab: ModuleManagerViewModel.Tabs,
    onTabSelected: (index: ModuleManagerViewModel.Tabs) -> Unit,
    onNavClicked: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.zIndex(4.dp.value),
        title = {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        TabItem(label = tab.label,
                            selected = selected, onTabSelected = {
                                onTabSelected(tab)
                            }
                        )
                    }
                }
            }
        }, navigationIcon = {
            IconButton(onClick = onNavClicked, content = {
                Icon(Icons.Filled.Menu)
            })
        }, backgroundColor = MaterialTheme.colors.surface
    )
}

@Composable
private fun TabItem(label: String, selected: Boolean, onTabSelected: () -> Unit) {
    Column(
        modifier = Modifier.selectable(
            selected = selected,
            onClick = onTabSelected,
            indication = null
        ).fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            color = animate(
                if (selected) MaterialTheme.colors.onSurface
                else MaterialTheme.colors.onSurface.copy(0.44f)
            )
        )
    }
}