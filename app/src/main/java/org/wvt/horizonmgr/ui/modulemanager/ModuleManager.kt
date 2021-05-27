package org.wvt.horizonmgr.ui.modulemanager

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import org.wvt.horizonmgr.ui.components.HorizonDivider
import org.wvt.horizonmgr.ui.fileselector.SharedFileChooserViewModel
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
    val managerViewModel: ModuleManagerViewModel = hiltViewModel()
    val moduleViewModel: ModTabViewModel = hiltViewModel()
    val icLevelViewModel: ICLevelTabViewModel = hiltViewModel()
    val mcLevelViewModel: MCLevelTabViewModel = hiltViewModel()
    val icResViewModel: ICResTabViewModel = hiltViewModel()
    val mcResViewModel: MCResTabViewModel = hiltViewModel()

    val sharedFileChooserViewModel = SharedFileChooserViewModel
    val selectedFile by sharedFileChooserViewModel.selected.collectAsState()

    LaunchedEffect(selectedFile) {
        selectedFile?.let {
            when (it.requestCode) {
                "add_mod" -> {
                    moduleViewModel.fileSelected(it.path)
                    sharedFileChooserViewModel.handledSelectedFile()
                }
                "ic_level" -> {
                    icLevelViewModel.selectedFileToInstall(it.path)
                    sharedFileChooserViewModel.handledSelectedFile()
                }
                "mc_level" -> {
                    mcLevelViewModel.selectedFileToInstall(it.path)
                    sharedFileChooserViewModel.handledSelectedFile()
                }
                "ic_texture" -> {
                    icResViewModel.selectedFileToInstall(it.path)
                    sharedFileChooserViewModel.handledSelectedFile()
                }
                "mc_texture" -> {
                    mcResViewModel.selectedFileToInstall(it.path)
                    sharedFileChooserViewModel.handledSelectedFile()
                }
            }
        }
    }

    ModuleManager(
        managerViewModel = managerViewModel,
        moduleViewModel = moduleViewModel,
        icLevelViewModel = icLevelViewModel,
        icResViewModel = icResViewModel,
        mcLevelViewModel = mcLevelViewModel,
        mcResViewModel = mcResViewModel,
        onNavClicked = onNavClicked,
        onAddModClicked = onAddModClicked,
        onAddICLevelClick = onAddICLevelClick,
        onAddMCLevelClick = onAddMCLevelClick,
        onAddICTextureClick = onAddICTextureClick,
        onAddMCTextureClick = onAddMCTextureClick
    )
}

@OptIn(ExperimentalPagerApi::class)
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
    val pagerState = rememberPagerState(pageCount = 5, initialOffscreenLimit = 5)

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            ModuleManagerViewModel.Tabs.MOD -> pagerState.animateScrollToPage(0)
            ModuleManagerViewModel.Tabs.IC_MAP -> pagerState.animateScrollToPage(1)
            ModuleManagerViewModel.Tabs.MC_MAP -> pagerState.animateScrollToPage(2)
            ModuleManagerViewModel.Tabs.IC_TEXTURE -> pagerState.animateScrollToPage(3)
            ModuleManagerViewModel.Tabs.MC_TEXTURE -> pagerState.animateScrollToPage(4)
        }
    }


    Column(Modifier.fillMaxSize()) {
        CustomAppBar(
            tabs = managerViewModel.tabs,
            selectedTab = selectedTab,
            onTabSelected = managerViewModel::selectTab,
            onNavClicked = onNavClicked
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            dragEnabled = false
        ) { page ->
            when (page) {
                0 -> ModTab(moduleViewModel, onAddModClicked)
                1 -> ICLevelTab(icLevelViewModel, onAddICLevelClick)
                2 -> MCLevelTab(mcLevelViewModel, onAddMCLevelClick)
                3 -> ICResTab(icResViewModel, onAddICTextureClick)
                4 -> MCResTab(mcResViewModel, onAddMCTextureClick)
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