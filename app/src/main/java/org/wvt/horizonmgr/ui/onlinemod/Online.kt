package org.wvt.horizonmgr.ui.onlinemod

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animate
import androidx.compose.foundation.AmbientContentColor
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.focus.ExperimentalFocus
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.id
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.ui.tooling.preview.Preview
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.components.DropDownSelector
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.main.SelectedPackageUUIDAmbient
import org.wvt.horizonmgr.ui.theme.HorizonManagerTheme

/*@Deprecated(level = DeprecationLevel.ERROR)
private fun List<WebAPI.OnlineModInfo>.sorted(mode: SortMode): List<WebAPI.OnlineModInfo> {
    return when (mode) {
        SortMode.DEFAULT -> sortedBy { it.index } // 服务端没有支持推荐排序，默认为最新模组
        SortMode.TIME_ASC -> sortedByDescending { it.id } // ID 越大代表
        SortMode.TIME_DSC -> sortedBy { it.id }
        SortMode.NAME_ASC -> sortedBy { it.name }
        SortMode.NAME_DSC -> sortedByDescending { it.name }
    }
}

@Deprecated(level = DeprecationLevel.ERROR)
private enum class SortMode(val label: String) {
    DEFAULT("推荐排序"), TIME_ASC("最新发布"), TIME_DSC("最先发布"),
    NAME_ASC("名称排序"), NAME_DSC("名称倒序")
}

@Deprecated(level = DeprecationLevel.ERROR)
private sealed class DownloadState {
    object NotLogin : DownloadState()
    object Loading : DownloadState()
    class Error(val message: String) : DownloadState()
    class OK(val items: List<WebAPI.OnlineModInfo>) : DownloadState()
}*/

@Composable
fun Online(
    enable: Boolean,
    onNavClicked: () -> Unit
) {
    val vm = dependenciesViewModel<OnlineViewModel>()
    val vmState by vm.listState.collectAsState()
    val vmOptions by vm.options.collectAsState()
    val vmDownloadState by vm.downloadState.collectAsState()
    val vmInstallState by vm.installState.collectAsState()
    val selectedPackageUUID = SelectedPackageUUIDAmbient.current

    onCommit(vm) {
        vm.setSelectedUUID(selectedPackageUUID)
    }

    /*// 该 List用于存储从服务器上获取的列表，提供给过滤器
    var list by remember { mutableStateOf<List<WebAPI.OnlineModInfo>>(emptyList()) }
*/
    /*var state by remember {
        mutableStateOf(
            if (enable) DownloadState.Loading
            else DownloadState.NotLogin
        )
    }*/


    // TODO 解耦
/*
    var selectedSource by remember { mutableStateOf(0) }

    val sortModes = remember { OnlineViewModel.SortMode.values().toList() }
    var selectedSortMode by remember { mutableStateOf(0) }
*/

    var filterValue by remember { mutableStateOf("") }

/*    fun load() {
        scope.launch {
            state = DownloadState.Loading
            val mods = try {
                (if (selectedSource == 0) {
                    webApi.getModsFromOfficial()
                } else webApi.getModsFromCN())
            } catch (e: WebAPI.WebAPIException) {
                state = DownloadState.Error(e.message)
                return@launch
            }
            list = mods
            val sorted = mods.sorted(sortModes[selectedSortMode])
            state = DownloadState.OK(sorted)
        }
    }

    onCommit(enable) {
        if (enable) load()
        else state = DownloadState.NotLogin
    }

    onCommit(selectedSource) {
        if (state !is DownloadState.OK) return@onCommit
        if (enable) load()
        else state = DownloadState.NotLogin
    }

    onCommit(selectedSortMode, filterValue) {
        if (state !is DownloadState.OK) return@onCommit
        val items = list
        state = DownloadState.Loading
        scope.launch {
            val filtered = if (filterValue.isNotBlank()) {
                items.filter {
                    it.name.contains(filterValue) || it.description.contains(filterValue)
                }
            } else items

            val sorted = filtered.sorted(sortModes[selectedSortMode])
            delay(200)
            state = DownloadState.OK(sorted)
        }
    }*/

    Column(Modifier.fillMaxSize()) {
        vmDownloadState?.let {
            ProgressDialog(onCloseRequest = { vm.downloadFinish() }, state = it)
        }
        vmInstallState?.let {
            ProgressDialog(onCloseRequest = { vm.installFinish() }, state = it)
        }
        TuneAppBar(
            onNavClicked = onNavClicked,
            onFilterValueConfirm = { vm.setFilterValue(it) },
            vm.sources, vmOptions.selectedSource, vm::setSelectedSource,
            vm.sortModes, vmOptions.selectedSortMode, vm::setSelectedSortMode
        )
        Crossfade(
            modifier = Modifier.fillMaxSize(),
            current = vmState
        ) {
            when (it) {
                OnlineViewModel.ListState.NotLogin -> {
                    Box(Modifier.fillMaxSize()) {
                        ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.medium) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = "此功能仅在登录后可用",
                                style = MaterialTheme.typography.h6
                            )
                        }
                    }
                }
                OnlineViewModel.ListState.Loading -> {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
                is OnlineViewModel.ListState.OK -> {
                    LazyColumnForIndexed(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 64.dp),
                        items = it.modList
                    ) { index, item ->
                        Item(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            title = item.name,
                            text = item.description,
                            imageUrl = item.iconUrl,
                            onDownloadClick = { vm.download(item) },
                            onInstallClick = { vm.install(item) }
                        )
                    }
                }
                is OnlineViewModel.ListState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(it.message)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = vm::refresh) {
                            Text("重试")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFocus::class)
@Composable
private fun TuneAppBar(
    onNavClicked: () -> Unit,
    onFilterValueConfirm: (value: String) -> Unit,
    sources: List<OnlineViewModel.Source>,
    selectedSource: OnlineViewModel.Source,
    onSourceSelect: (index: OnlineViewModel.Source) -> Unit,
    sortModes: List<OnlineViewModel.SortMode>,
    selectedSortMode: OnlineViewModel.SortMode,
    onSortModeSelect: (index: OnlineViewModel.SortMode) -> Unit
) {
    var filterValue by remember { mutableStateOf(TextFieldValue()) }
    var expand by remember { mutableStateOf(false) }
    val emphasis = AmbientEmphasisLevels.current
    val focus = remember { FocusRequester() }

    ExpandableAppBarLayout(
        onNavClicked = onNavClicked,
        onActionClick = { expand = !expand },
        expand = expand,
        title = { Text(text = "在线下载") }
    ) {
        ProvideEmphasis(emphasis = emphasis.medium) {
            Column(
                Modifier.padding(
                    top = 8.dp,
                    start = 32.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
            ) {
                // 搜索框
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(asset = Icons.Filled.Search)
                    TextField(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                            .weight(1f)
                            .focusRequester(focus)
                            .focusObserver { onFilterValueConfirm(filterValue.text) },
                        value = filterValue, onValueChange = { filterValue = it },
                        label = { Text("搜索") }
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(asset = Icons.Filled.Language)
                    DropDownSelector(
                        modifier = Modifier.padding(start = 16.dp),
                        items = sources.map { it.label },
                        selectedIndex = sources.indexOf(selectedSource),
                        onSelected = { onSourceSelect(sources[it]) }
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(asset = Icons.Filled.Sort)
                    DropDownSelector(
                        modifier = Modifier.padding(start = 16.dp),
                        items = sortModes.map { it.label },
                        selectedIndex = sortModes.indexOf(selectedSortMode),
                        onSelected = { onSortModeSelect(sortModes[it]) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableAppBarLayout(
    onNavClicked: () -> Unit,
    onActionClick: () -> Unit,
    expand: Boolean,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val progress = animate(if (expand) 1f else 0f)
    Surface(
        elevation = 4.dp,
        color = MaterialTheme.colors.surface
    ) {
        Layout(children = {
            TopAppBar(
                modifier = Modifier.layoutId("appbar"),
                navigationIcon = {
                    IconButton(onClick = onNavClicked) {
                        Icon(asset = Icons.Filled.Menu)
                    }
                },
                title = title,
                actions = {
                    IconButton(onClick = onActionClick) {
                        Crossfade(current = expand) {
                            if (it) Icon(asset = Icons.Filled.Close)
                            else Icon(asset = Icons.Filled.FilterAlt)
                        }
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp
            )
            Surface(
                modifier = Modifier.fillMaxWidth().drawOpacity(progress).layoutId("content"),
                color = Color.Transparent,
                content = content
            )
        }, measureBlock = { measurables, constraints ->
            if (measurables.size != 2) error("Need 2 children")

            if (progress == 0f) { // Not expand
                val appbar = measurables.first { it.id == "appbar" }.measure(constraints)
                val appbarHeight = appbar.height
                layout(
                    constraints.maxWidth,
                    appbarHeight
                ) {
                    appbar.placeRelative(0, 0) // TopAppBar
                }
            } else { // Expanding | Expanded
                val appbar = measurables.first { it.id == "appbar" }.measure(constraints)
                val appbarHeight = appbar.height

                val mContent = measurables.first { it.id == "content" }.measure(constraints)
                val mContentHeight = mContent.height

                // 在 appbarHeight 和 appbarHeight + mContentHeight 之间变换
                val height = lerp(appbarHeight, appbarHeight + mContentHeight, progress)

                layout(
                    constraints.maxWidth,
                    height
                ) {
                    appbar.placeRelative(0, 0) // TopAppBar
                    mContent.placeRelative(0, appbarHeight) // Content
                }
            }
        })
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Item(
    title: String, text: String, imageUrl: String, modifier: Modifier = Modifier,
    onDownloadClick: () -> Unit,
    onInstallClick: () -> Unit
) {
    Card(modifier = modifier, elevation = 2.dp) {
        Column(
            Modifier.padding(top = 16.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
        ) {
            Row {
                Column(Modifier.weight(1f).padding(end = 8.dp)) {
                    // Title
                    Text(title, style = MaterialTheme.typography.h5)
                    // Secondary Text
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = text, style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
                // Image
                NetworkImage(
                    url = imageUrl,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(4.dp))
                )
            }
            Row(
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Providers(AmbientContentColor provides MaterialTheme.colors.primary) {
                    IconButton(onClick = onInstallClick) {
                        Icon(Icons.Filled.Extension)
                    }
                    IconButton(
                        onClick = onDownloadClick,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.Filled.GetApp)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ItemPreview() {
    HorizonManagerTheme {
        Surface {
            Item(
                "Example Mod",
                "Example description",
                "http://127.0.0.1",
                onDownloadClick = {},
                onInstallClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}