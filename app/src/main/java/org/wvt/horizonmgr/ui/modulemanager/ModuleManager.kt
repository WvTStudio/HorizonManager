package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.components.HorizonDivider
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor
import org.wvt.horizonmgr.viewmodel.*

@Composable
fun ModuleManagerScreen(
    onNavClicked: () -> Unit,
    onAddModClicked: () -> Unit,
    onAddICLevelClick: () -> Unit,
    onAddMCLevelClick: () -> Unit,
    onAddICTextureClick: () -> Unit,
    onAddMCTextureClick: () -> Unit
) {
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

private enum class Tabs {
    MOD, HZ_MAP, MC_MAP, HZ_RESPACK, MC_RESPACK
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ModuleManager(
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
    var selectedTab by rememberSaveable { mutableStateOf(Tabs.MOD) }
    val pagerState = rememberPagerState(pageCount = 5, initialOffscreenLimit = 5)

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            Tabs.MOD -> pagerState.animateScrollToPage(0)
            Tabs.HZ_MAP -> pagerState.animateScrollToPage(1)
            Tabs.MC_MAP -> pagerState.animateScrollToPage(2)
            Tabs.HZ_RESPACK -> pagerState.animateScrollToPage(3)
            Tabs.MC_RESPACK -> pagerState.animateScrollToPage(4)
        }
    }


    Column(Modifier.fillMaxSize()) {
        CustomAppBar(
            tabs = remember { Tabs.values().toList() },
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
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

@Stable
@Composable
private fun Tabs.getLabel(): String = when (this) {
    Tabs.MOD -> stringResource(R.string.mm_screen_tab_mod)
    Tabs.HZ_MAP -> stringResource(R.string.mm_screen_tab_hzmap)
    Tabs.MC_MAP -> stringResource(R.string.mm_screen_tab_mcmap)
    Tabs.HZ_RESPACK -> stringResource(R.string.mm_screen_tab_hzres)
    Tabs.MC_RESPACK -> stringResource(R.string.mm_screen_tab_mcres)
}

@Composable
private fun CustomAppBar(
    tabs: List<Tabs>,
    selectedTab: Tabs,
    onTabSelected: (index: Tabs) -> Unit,
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
                            label = tab.getLabel(),
                            selected = selected,
                            onTabSelected = { onTabSelected(tab) }
                        )
                    }
                }
            }
        }, navigationIcon = {
            IconButton(onClick = onNavClicked, content = {
                Icon(Icons.Rounded.Menu, stringResource(R.string.navigation_action_menu))
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