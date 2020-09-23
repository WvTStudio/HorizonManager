package org.wvt.horizonmgr.ui.onlinemod

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animate
import androidx.compose.foundation.ContentColorAmbient
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.ui.HorizonManagerAmbient
import org.wvt.horizonmgr.ui.WebAPIAmbient
import org.wvt.horizonmgr.ui.components.DropDownSelector
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import org.wvt.horizonmgr.ui.main.DrawerStateAmbient
import org.wvt.horizonmgr.ui.main.SelectedPackageUUIDAmbient
import org.wvt.horizonmgr.ui.main.UserInfoAmbient
import org.wvt.horizonmgr.ui.theme.HorizonManagerTheme

private fun List<WebAPI.OnlineModInfo>.sorted(mode: SortMode): List<WebAPI.OnlineModInfo> {
    return when (mode) {
        SortMode.DEFAULT -> sortedBy { it.index } // 服务端没有支持推荐排序，默认为最新模组
        SortMode.TIME_ASC -> sortedByDescending { it.id } // ID 越大代表
        SortMode.TIME_DSC -> sortedBy { it.id }
        SortMode.NAME_ASC -> sortedBy { it.name }
        SortMode.NAME_DSC -> sortedByDescending { it.name }
    }
}

private enum class SortMode(val label: String) {
    DEFAULT("推荐排序"), TIME_ASC("最新发布"), TIME_DSC("最先发布"),
    NAME_ASC("名称排序"), NAME_DSC("名称倒序")
}

private sealed class DownloadState {
    object NotLogin : DownloadState()
    object Loading : DownloadState()
    class Error(val message: String) : DownloadState()
    class OK(val items: List<WebAPI.OnlineModInfo>) : DownloadState()
}

@Composable
fun Online(onDownloadClick: (WebAPI.OnlineModInfo) -> Unit) {
    val horizonMgr = HorizonManagerAmbient.current
    val webApi = WebAPIAmbient.current

    val scope = rememberCoroutineScope()
    val userInfo = UserInfoAmbient.current

    // 该 List用于存储从服务器上获取的列表，提供给过滤器
    var list by remember { mutableStateOf<List<WebAPI.OnlineModInfo>>(emptyList()) }

    var state by remember {
        mutableStateOf<DownloadState>(
            if (userInfo == null) DownloadState.NotLogin
            else DownloadState.Loading
        )
    }

    // TODO 解耦
    val sources = remember { listOf("官方源", "汉化组源") }
    var selectedSource by remember { mutableStateOf(0) }

    val sortModes = remember { SortMode.values() }
    var selectedSortMode by remember { mutableStateOf(0) }

    val selectedPackageUUID = SelectedPackageUUIDAmbient.current

    var progressDialogState by remember { mutableStateOf<ProgressDialogState?>(null) }

    var filterValue by remember { mutableStateOf<String>("") }

    fun load() {
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

    onCommit(userInfo) {
        if (userInfo != null) {
            load()
        } else {
            state = DownloadState.NotLogin
        }
    }

    onCommit(selectedSource) {
        if (state !is DownloadState.OK) return@onCommit
        if (userInfo != null) {
            load()
        } else {
            state = DownloadState.NotLogin
        }
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
    }

    Column(Modifier.fillMaxSize()) {
        progressDialogState?.let {
            ProgressDialog(onCloseRequest = { progressDialogState = null }, state = it)
        }
        TuneAppBar(
            onFilterValueConfirm = { filterValue = it },
            sources, selectedSource, { selectedSource = it },
            sortModes, selectedSortMode, { selectedSortMode = it }
        )
        Crossfade(current = state) {
            when (it) {
                DownloadState.NotLogin -> {
                    Stack(Modifier.fillMaxSize()) {
                        ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = "此功能仅在登录后可用",
                                style = MaterialTheme.typography.h6
                            )
                        }
                    }
                }
                DownloadState.Loading -> {
                    Stack(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
                is DownloadState.OK -> {
                    LazyColumnForIndexed(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 64.dp),
                        items = it.items
                    ) { index, item ->
                        Item(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            title = item.name,
                            text = item.description,
                            imageUrl = item.iconUrl,
                            onDownloadClick = {
                                // TODO 解耦并增加反馈
                                scope.launch {
                                    progressDialogState = ProgressDialogState.Loading("正在下载")
                                    try {
                                        webApi.downloadMod(item).await()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        progressDialogState =
                                            ProgressDialogState.Failed(
                                                "下载失败",
                                                e.localizedMessage ?: ""
                                            )
                                        return@launch
                                    }
                                    progressDialogState = ProgressDialogState.Finished("下载完成")
                                }
                            },
                            onInstallClick = {
                                // TODO 解耦并增加反馈
                                scope.launch {
                                    selectedPackageUUID?.let {
                                        progressDialogState = ProgressDialogState.Loading("正在下载安装")
                                        try {
                                            val mod = webApi.downloadMod(item).await()
                                            horizonMgr.installMod(it, mod)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            progressDialogState = ProgressDialogState.Failed(
                                                "安装失败",
                                                e.localizedMessage ?: ""
                                            )
                                            return@launch
                                        }
                                        progressDialogState = ProgressDialogState.Finished("安装成功")
                                    }
                                }
                            }
                        )
                    }
                }
                is DownloadState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(it.message)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = ::load) {
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
    onFilterValueConfirm: (value: String) -> Unit,
    sources: List<String>, selectedSource: Int, onSourceSelect: (index: Int) -> Unit,
    sortModes: Array<SortMode>, selectedSortMode: Int, onSortModeSelect: (index: Int) -> Unit
) {
    var filterValue by remember { mutableStateOf(TextFieldValue()) }
    val drawerState = DrawerStateAmbient.current
    var expand by remember { mutableStateOf(false) }
    val emphasis = EmphasisAmbient.current
    val focus: FocusRequester = remember { FocusRequester() }

    val progress = animate(if (expand) 1f else 0f)

    Surface(
        elevation = 4.dp,
        color = MaterialTheme.colors.surface
    ) {
        Layout(
            children = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { drawerState.open() }) {
                            Icon(asset = Icons.Filled.Menu)
                        }
                    },
                    title = {
                        Text(text = "在线下载")
                    },
                    actions = {
                        IconButton(onClick = {
                            expand = !expand
                        }) {
                            Crossfade(current = expand) {
                                if (it) Icon(Icons.Filled.Close)
                                else Icon(asset = Icons.Filled.FilterAlt)
                            }
                        }
                    },
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp
                )
                Surface(
                    modifier = Modifier.fillMaxWidth().drawOpacity(progress),
                    color = Color.Transparent
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(asset = Icons.Filled.Search)
                                TextField(
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                                        .weight(1f)
                                        .focusRequester(focus).focusObserver {
                                            onFilterValueConfirm(filterValue.text)
                                        },
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
                                    items = sources,
                                    selectedIndex = selectedSource,
                                    onSelected = onSourceSelect
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
                                    selectedIndex = selectedSortMode,
                                    onSelected = onSortModeSelect
                                )
                            }
                        }
                    }
                }
            }, measureBlock = { measurables, constraints ->
                if (measurables.size != 2) error("Need 2 children")

                if (progress == 0f) { // Not expanded
                    val appbar = measurables[0].measure(constraints)
                    val appbarHeight = appbar.height

                    layout(
                        constraints.maxWidth,
                        appbarHeight
                    ) {
                        appbar.placeRelative(0, 0) // TopAppBar
                    }
                } else { // Expanded
                    val appbar = measurables[0].measure(constraints)
                    val appbarHeight = appbar.height

                    val tune = measurables[1].measure(constraints)
                    val tuneHeight = tune.height

                    val height = lerp(appbarHeight, appbarHeight + tuneHeight, progress)

                    layout(
                        constraints.maxWidth,
                        height
                    ) {
                        appbar.placeRelative(0, 0) // TopAppBar
                        tune.placeRelative(0, appbarHeight) // Content
                    }
                }
            }
        )
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
                Providers(ContentColorAmbient provides MaterialTheme.colors.primary) {
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