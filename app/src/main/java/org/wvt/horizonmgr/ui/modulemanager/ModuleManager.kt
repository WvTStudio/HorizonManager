package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.zIndex
import org.wvt.horizonmgr.ui.components.HorizonDivider
import org.wvt.horizonmgr.ui.main.LocalSelectedPackageUUID

@Composable
fun ModuleManager(
    managerViewModel: ModuleManagerViewModel,
    moduleViewModel: ModTabViewModel,
    icLevelViewModel: ICLevelTabViewModel,
    mcLevelViewModel: MCLevelTabViewModel,
    onNavClicked: () -> Unit,
    onAddModClicked: () -> Unit
) {
    val selectedTab by managerViewModel.selectedTab.collectAsState()
    val pkgId = LocalSelectedPackageUUID.current

    DisposableEffect(pkgId) {
        moduleViewModel.setSelectedUUID(pkgId)
        moduleViewModel.load()

        icLevelViewModel.setPackage(pkgId)
        icLevelViewModel.load()
        onDispose {}
    }

    DisposableEffect(Unit) {
        mcLevelViewModel.load()
        onDispose {  }
    }

    Column {
        CustomAppBar(
            tabs = managerViewModel.tabs,
            selectedTab = selectedTab,
            onTabSelected = managerViewModel::selectTab,
            onNavClicked = onNavClicked
        )
        Box(Modifier.fillMaxSize()) {
            Crossfade(selectedTab) {
                when (it) {
                    ModuleManagerViewModel.Tabs.MOD -> ModTab(moduleViewModel, onAddModClicked)
                    ModuleManagerViewModel.Tabs.IC_MAP -> ICLevelTab(icLevelViewModel)
                    ModuleManagerViewModel.Tabs.MC_MAP -> MCLevelTab(mcLevelViewModel)
                    ModuleManagerViewModel.Tabs.IC_TEXTURE -> MCResTab()
                    ModuleManagerViewModel.Tabs.MC_TEXTURE -> ICResTab()
                }
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
                Icon(Icons.Filled.Menu, contentDescription = "菜单")
            })
        }, backgroundColor = MaterialTheme.colors.surface
    )
}

@Composable
private fun TabItem(label: String, selected: Boolean, onTabSelected: () -> Unit) {
    Column(
        modifier = Modifier
            .selectable(
                selected = selected,
                onClick = onTabSelected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            color = animateColorAsState(
                if (selected) MaterialTheme.colors.onSurface
                else MaterialTheme.colors.onSurface.copy(0.44f)
            ).value
        )
    }
}