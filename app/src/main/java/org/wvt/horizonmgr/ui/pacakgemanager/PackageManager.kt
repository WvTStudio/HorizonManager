package org.wvt.horizonmgr.ui.pacakgemanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.wvt.horizonmgr.ui.components.*
import org.wvt.horizonmgr.ui.main.LocalSelectedPackageUUID
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor
import org.wvt.horizonmgr.ui.theme.PreviewTheme
import kotlin.coroutines.resume

data class PackageManagerItem(
    val uuid: String,
    val name: String,
    val timeStr: String,
    val description: String
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun PackageManager(
    viewModel: PackageManagerViewModel,
    onPackageSelect: (uuid: String?) -> Unit,
    navigateToPackageInfo: (uuid: String) -> Unit,
    onOnlineInstallClick: () -> Unit,
    onLocalInstallClick: () -> Unit,
    onNavClick: () -> Unit
) {
    val selectedPackageUUID = LocalSelectedPackageUUID.current

    DisposableEffect(Unit) {
        viewModel.loadPackages()
        onDispose { }
    }

    DisposableEffect(selectedPackageUUID) {
        viewModel.setSelectedPackage(selectedPackageUUID)
        onDispose {}
    }

    val packages by viewModel.packages.collectAsState()
    val progresstate by viewModel.progressState.collectAsState()

    val errors by viewModel.errors.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val confirmDeleteDialogHostState = remember { ConfirmDeleteDialogHostState() }
    val inputDialogHostState = remember { InputDialogHostState() }
    var fabExpand by rememberSaveable { mutableStateOf(false) }

    progresstate?.let {
        ProgressDialog(onCloseRequest = viewModel::dismiss, state = it)
    }

    val banner = @Composable {
        ErrorBanner(
            modifier = Modifier.fillMaxWidth(),
            errors = errors,
            text = "解析分包时发生 ${errors.size} 个错误"
        )
    }

    Box {
        Column {
            var showMenu by remember { mutableStateOf(false) }
            // Top App Bar
            TopAppBar(
                modifier = Modifier.zIndex(4.dp.value),
                title = {
                    Text("分包管理")
                }, navigationIcon = {
                    IconButton(onClick = onNavClick) { Icon(Icons.Filled.Menu, "菜单") }
                }, backgroundColor = AppBarBackgroundColor,
                actions = {
                    Box {
                        // Menu Icon
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, "更多")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // Menu
                            DropdownMenuItem(onClick = {
                                viewModel.loadPackages()
                                showMenu = false
                            }) { Text("刷新") }
                        }
                    }
                }
            )

            if (packages.isNullOrEmpty()) {
                // Tips when there was no package installed.
                EmptyPage(Modifier.fillMaxSize()) { Text("你还没有安装分包") }
                banner()
            } else LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { banner() }
                item { Spacer(Modifier.height(8.dp)) }
                itemsIndexed(packages) { index, item ->
                    PackageItem(
                        modifier = Modifier.padding(16.dp, 8.dp),
                        title = item.name,
                        description = item.description,
                        installTime = item.timeStr,
                        selected = item.uuid == selectedPackageUUID,
                        onClick = { onPackageSelect(item.uuid) },
                        onInfoClick = { navigateToPackageInfo(item.uuid) },
                        onDeleteClick = {
                            viewModel.deletePackage(item.uuid,
                                confirmDeleteDialogHostState,
                                { onPackageSelect(null) }
                            )
                        },
                        onRenameClick = {
                            viewModel.renamePackage(
                                item.uuid,
                                inputDialogHostState
                            )
                        },
                        onCloneClick = { viewModel.clonePackage(item.uuid, inputDialogHostState) }
                    )
                }
                item { Spacer(Modifier.height(64.dp)) }
            }
        }

        SnackbarHost(hostState = snackbarHostState)

        // Displays when user is deleting a package
        ConfirmDeleteDialogHost(state = confirmDeleteDialogHostState)

        // Displays when user is renaming/cloning a package
        InputDialogHost(state = inputDialogHostState)

        // Floating Actions Bars
        FABs(
            modifier = Modifier.fillMaxSize(),
            expand = fabExpand,
            onExpandStateChange = { fabExpand = it },
            onLocalInstallClick = {
                onLocalInstallClick()
                fabExpand = false
            },
            onOnlineInstallClick = {
                onOnlineInstallClick()
                fabExpand = false
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun <T> PackageManagerUI(
    onNavClick: () -> Unit,
    packages: List<T>,
    fabContent: @Composable () -> Unit,
    emptyPage: @Composable () -> Unit,
    item: @Composable (index: Int, item: T) -> Unit,
    displayConfirmDialog: Boolean,
    onConfirmDialogConfirm: () -> Unit,
    onConfirmDialogCancel: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val confirmDeleteDialogHostState = remember { ConfirmDeleteDialogHostState() }
    val inputDialogHostState = remember { InputDialogHostState() }

    Box {
        Column {
            // Top App Bar
            TopAppBar(
                modifier = Modifier.zIndex(4.dp.value),
                title = {
                    Text("分包管理")
                }, navigationIcon = {
                    IconButton(onClick = onNavClick) { Icon(Icons.Filled.Menu, "菜单") }
                }, backgroundColor = MaterialTheme.colors.surface
            )

            if (packages.isEmpty()) {
                Box(Modifier.fillMaxSize()) {
                    emptyPage()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 64.dp)
                ) {
                    itemsIndexed(packages) { index, item ->
                        item(index, item)
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState)
        ConfirmDeleteDialogHost(state = confirmDeleteDialogHostState)
        InputDialogHost(state = inputDialogHostState)

        Box(Modifier.align(Alignment.BottomEnd)) {
            fabContent()
        }
    }
}

private val fab1Enter = tween<Float>(100, 0, LinearOutSlowInEasing)
private val fab2Enter = tween<Float>(100, 50, LinearOutSlowInEasing)

private val fab1Exit = tween<Float>(80, 40, FastOutLinearInEasing)
private val fab2Exit = tween<Float>(80, 0, FastOutLinearInEasing)

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun FABs(
    modifier: Modifier = Modifier,
    expand: Boolean,
    onExpandStateChange: (expand: Boolean) -> Unit,
    onLocalInstallClick: () -> Unit,
    onOnlineInstallClick: () -> Unit
) {
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(horizontalAlignment = Alignment.End) {
            FABEntry(
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp),
                expand = expand,
                enterAnim = fab2Enter,
                exitAnim = fab2Exit,
                text = { Text("在线安装") },
                icon = { Icon(Icons.Filled.CloudDownload, null) },
                onClick = onOnlineInstallClick
            )
            FABEntry(
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp),
                expand = expand,
                enterAnim = fab1Enter,
                exitAnim = fab1Exit,
                text = { Text("本地导入") },
                icon = { Icon(Icons.Filled.Storage, null) },
                onClick = onLocalInstallClick
            )
            FloatingActionButton(
                modifier = Modifier.padding(top = 16.dp),
                onClick = { onExpandStateChange(!expand) }
            ) {
                val rotate = animateFloatAsState(if (expand) 45f else 0f).value
                // Rotate 45° to be Close Icon
                // TODO: 2020/11/13 Use animation icon to instead this.
                Icon(
                    modifier = Modifier.graphicsLayer(rotationZ = rotate),
                    imageVector = Icons.Filled.Add,
                    contentDescription = "添加"
                )
            }
        }
    }
}

@Composable
private fun FABEntry(
    modifier: Modifier = Modifier,
    expand: Boolean,
    enterAnim: FiniteAnimationSpec<Float>,
    exitAnim: FiniteAnimationSpec<Float>,
    text: @Composable BoxScope.() -> Unit,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val transition by updateTransition(expand, label = "progress").animateFloat(
        transitionSpec = {
            if (targetState) enterAnim else exitAnim
        }, targetValueByState = {
            if (it) 1f else 0f
        }
    )

    if (transition > 0f) {
        Row(modifier = modifier.wrapContentSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.graphicsLayer(
                    alpha = transition,
                    scaleY = transition,
                    scaleX = transition,
                    clip = false
                )
            ) {
                Card(
                    modifier = Modifier
                        .height(40.dp)
                        .padding(vertical = 4.dp, horizontal = 16.dp)
                        .wrapContentWidth(),
                    elevation = 2.dp
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                            content = text
                        )
                    }
                }
            }
            Box(
                Modifier.graphicsLayer(
                    alpha = transition,
                    scaleY = transition,
                    scaleX = transition,
                    clip = false
                )
            ) {
                FloatingActionButton(
                    modifier = Modifier.size(40.dp),
                    backgroundColor = MaterialTheme.colors.surface,
                    onClick = onClick,
                    content = icon
                )
            }
        }

    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PackageItem(
    title: String,
    description: String,
    installTime: String,
    selected: Boolean,
    onClick: () -> Unit,
    onInfoClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRenameClick: () -> Unit,
    onCloneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Card(
        modifier = modifier,
        elevation = animateDpAsState(if (pressed) 8.dp else 1.dp).value
    ) {
        Column(
            Modifier.clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = LocalIndication.current
            )
        ) {
            // Body
            Row {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    // Title
                    Text(title, style = MaterialTheme.typography.h6)
                    // Description
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = description,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Actions
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Row(Modifier.padding(top = 8.dp, end = 4.dp)) {
                        var dropdown by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { dropdown = true }) {
                                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "更多")
                            }
                            DropdownMenu(
                                expanded = dropdown,
                                onDismissRequest = { dropdown = false }) {
                                DropdownMenuItem(onClick = {
                                    dropdown = false
                                    onInfoClick()
                                }) {
                                    Text("详情")
                                }
                                DropdownMenuItem(onClick = {
                                    dropdown = false
                                    onDeleteClick()
                                }) {
                                    Text("删除")
                                }
                                DropdownMenuItem(onClick = {
                                    dropdown = false
                                    onCloneClick()
                                }) {
                                    Text("克隆")
                                }
                                DropdownMenuItem(onClick = {
                                    dropdown = false
                                    onRenameClick()
                                }) {
                                    Text("重命名")
                                }
                            }

                        }
                    }
                }
            }
            // Footer
            Row(
                modifier = Modifier
                    .padding(
                        top = 4.dp,
                        bottom = 8.dp,
                        start = 16.dp,
                        end = 8.dp
                    )
                    .fillMaxWidth()
                    .heightIn(min = 32.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.primary) {
                    AnimatedVisibility(
                        visible = selected,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Icon(Icons.Filled.CheckCircle, "已选择")
                    }
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(text = installTime, style = MaterialTheme.typography.caption)
                    }
                }
            }
        }
    }
}

class ConfirmDeleteDialogHostState {
    var currentData by mutableStateOf<Data?>(null)
        private set

    private val mutex = Mutex()

    suspend fun showDialog(): DialogResult = mutex.withLock {
        try {
            return suspendCancellableCoroutine { cont ->
                currentData = Data(cont)
            }
        } finally {
            currentData = null
        }
    }

    @Stable
    class Data(
        private val continuation: CancellableContinuation<DialogResult>
    ) {
        fun confirm() {
            if (continuation.isActive) continuation.resume(DialogResult.CONFIRM)
        }

        fun dismiss() {
            if (continuation.isActive) continuation.resume(DialogResult.DISMISS)
        }
    }

    enum class DialogResult {
        CONFIRM, DISMISS
    }
}

@Composable
fun ConfirmDeleteDialogHost(state: ConfirmDeleteDialogHostState) {
    val data = state.currentData
    if (data != null) {
        AlertDialog(onDismissRequest = {
            data.dismiss()
        }, title = {
            Text("是否确认删除")
        }, text = {
            Text("分包内的模组、地图、材质都将一并删除，且无法恢复！")
        }, dismissButton = {
            TextButton(onClick = { data.dismiss() }) { Text("取消") }
        }, confirmButton = {
            TextButton(onClick = { data.confirm() }) { Text("删除") }
        })
    }
}

@Preview
@Composable
private fun Pareview() {
    var fabExpand by remember { mutableStateOf(false) }
    PreviewTheme {
        PackageManagerUI(
            onNavClick = { /*TODO*/ },
            packages = (0..10).toList(),
            fabContent = {
                FABs(
                    modifier = Modifier.fillMaxSize(),
                    expand = fabExpand,
                    onExpandStateChange = { fabExpand = it },
                    onLocalInstallClick = { /*TODO*/ },
                    onOnlineInstallClick = { /*TODO*/ }
                )
            },
            emptyPage = {},
            item = { index, item ->
                PackageItem(
                    modifier = Modifier.padding(16.dp, 8.dp),
                    title = "Example Item",
                    description = "Example description",
                    installTime = "unknown",
                    selected = false,
                    onClick = { /*TODO*/ },
                    onInfoClick = { /*TODO*/ },
                    onDeleteClick = { /*TODO*/ },
                    onRenameClick = { /*TODO*/ },
                    onCloneClick = { /*TODO*/ })
            },
            displayConfirmDialog = false,
            onConfirmDialogConfirm = { /*TODO*/ },
            onConfirmDialogCancel = { /*TODO*/ }
        )
    }
}