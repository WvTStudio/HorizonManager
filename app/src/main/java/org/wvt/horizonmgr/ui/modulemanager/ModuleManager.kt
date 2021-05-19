package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import org.wvt.horizonmgr.ui.components.HorizonDivider
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor

@Composable
fun ModuleManagerScreen(
    onNavClicked: () -> Unit,
    onAddModClicked: () -> Unit,
    onAddICLevelClick: () -> Unit,
    onAddMCLevelClick: () -> Unit,
    onAddICTextureClick: () -> Unit,
    onAddMCTextureClick: () -> Unit
) {
    ModuleManager(
        managerViewModel = hiltViewModel(),
        moduleViewModel = hiltViewModel(),
        icLevelViewModel = hiltViewModel(),
        icResViewModel = hiltViewModel(),
        mcLevelViewModel = hiltViewModel(),
        mcResViewModel = hiltViewModel(),
        onNavClicked = onNavClicked,
        onAddModClicked = onAddModClicked,
        onAddICLevelClick = onAddICLevelClick,
        onAddMCLevelClick = onAddMCLevelClick,
        onAddICTextureClick = onAddICTextureClick,
        onAddMCTextureClick = onAddMCTextureClick
    )
}

@Composable
fun ModuleManager(
    managerViewModel: ModuleManagerViewModel,
    moduleViewModel: ModTabViewModel,
    icLevelViewModel: ICLevelTabViewModel,
    icResViewModel: ICResTabViewModel,
    mcLevelViewModel: MCLevelTabViewModel,
    mcResViewModel: MCResTabViewModel,
    onNavClicked: () -> Unit,
    onAddModClicked: () -> Unit,
    onAddICLevelClick: () -> Unit,
    onAddMCLevelClick: () -> Unit,
    onAddICTextureClick: () -> Unit,
    onAddMCTextureClick: () -> Unit
) {
    val selectedTab by managerViewModel.selectedTab.collectAsState()

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
                    ModuleManagerViewModel.Tabs.IC_MAP -> ICLevelTab(
                        icLevelViewModel,
                        onAddICLevelClick
                    )
                    ModuleManagerViewModel.Tabs.MC_MAP -> MCLevelTab(
                        mcLevelViewModel,
                        onAddMCLevelClick
                    )
                    ModuleManagerViewModel.Tabs.IC_TEXTURE -> ICResTab(
                        icResViewModel,
                        onAddICTextureClick
                    )
                    ModuleManagerViewModel.Tabs.MC_TEXTURE -> MCResTab(
                        mcResViewModel,
                        onAddMCTextureClick
                    )
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
                HorizonDivider(modifier = Modifier.height(32.dp))
                ScrollableTabRow(
                    modifier = Modifier.fillMaxHeight(),
                    selectedTabIndex = tabs.indexOf(selectedTab),
                    edgePadding = 0.dp,
                    backgroundColor = Color.Transparent,
                    contentColor = contentColorFor(AppBarBackgroundColor),
                    indicator = {},
                    divider = {}
                ) {
                    tabs.fastForEachIndexed { index, tab ->
                        val selected = tab == selectedTab
                        TabItem(
                            label = tab.label,
                            selected = selected,
                            onTabSelected = { onTabSelected(tab) }
                        )
                    }
                }
            }
        }, navigationIcon = {
            IconButton(onClick = onNavClicked, content = {
                Icon(Icons.Filled.Menu, contentDescription = "菜单")
            })
        }, backgroundColor = AppBarBackgroundColor
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
        val color = LocalContentColor.current
        Text(
            text = label,
            color = animateColorAsState(
                if (selected) color
                else color.copy(ContentAlpha.disabled)
            ).value
        )
    }
}