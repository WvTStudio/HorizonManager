package org.wvt.horizonmgr.ui.pacakgemanager

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.components.InputDialogHost
import org.wvt.horizonmgr.ui.components.InputDialogHostState
import org.wvt.horizonmgr.ui.components.MyAlertDialog
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.main.AmbientSelectedPackageUUID
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
    onPackageSelect: (uuid: String?) -> Unit,
    onNavClick: () -> Unit
) {
    val context = AmbientContext.current as AppCompatActivity

    val vm = dependenciesViewModel<PackageManagerViewModel>()
    val selectedPackageUUID = AmbientSelectedPackageUUID.current
    onCommit(selectedPackageUUID) {
        vm.setSelectedPackage(selectedPackageUUID)
    }

    val vmPackages by vm.packages.collectAsState()
    val vmPs by vm.progressState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val confirmDeleteDialogHostState = remember { ConfirmDeleteDialogHostState() }
    val inputDialogHostState = remember { InputDialogHostState() }
    var fabExpand by savedInstanceState { false }

    vmPs?.let {
        ProgressDialog(onCloseRequest = vm::dismiss, state = it)
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
                    IconButton(onClick = onNavClick) { Icon(Icons.Filled.Menu) }
                }, backgroundColor = MaterialTheme.colors.surface,
                actions = {
                    DropdownMenu(
                        toggle = {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Filled.MoreVert)
                            }
                        },
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            vm.loadPackages()
                            showMenu = false
                        }) {
                            Text("刷新")
                        }
                    }
                }
            )

            if (vmPackages.isNullOrEmpty()) {
                // Tips when there was no package installed.
                EmptyPage()
            } else LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 64.dp)
            ) {
                itemsIndexed(vmPackages) { index, item ->
                    PackageItem(
                        modifier = Modifier.padding(16.dp, 8.dp),
                        title = item.name,
                        description = item.description,
                        installTime = item.timeStr,
                        selected = item.uuid == selectedPackageUUID,
                        onClick = { onPackageSelect(item.uuid) },
                        onInfoClick = { vm.showInfo(context, item.uuid) },
                        onDeleteClick = {
                            vm.deletePackage(
                                item.uuid,
                                confirmDeleteDialogHostState
                            )
                        },
                        onRenameClick = { vm.renamePackage(item.uuid, inputDialogHostState) },
                        onCloneClick = { vm.clonePackage(item.uuid, inputDialogHostState) }
                    )
                }
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
                // TODO: 2020/11/13
                fabExpand = false
            },
            onOnlineInstallClick = {
                vm.startInstallPackageActivity(context)
                fabExpand = false
            }
        )
    }
}

@Composable
private fun EmptyPage() {
    Box(Modifier.fillMaxSize()) {
        Row(Modifier.align(Alignment.Center)) {
            Text("你还没有安装分包")
        }
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
                    IconButton(onClick = onNavClick) { Icon(Icons.Filled.Menu) }
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

private val fab1Enter = tween<Float>(100, 0, FastOutSlowInEasing)
private val fab2Enter = tween<Float>(100, 40, FastOutSlowInEasing)

private val fab1Exit = tween<Float>(100, 40, FastOutSlowInEasing)
private val fab2Exit = tween<Float>(100, 0, FastOutSlowInEasing)

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
                icon = { Icon(Icons.Filled.CloudDownload) },
                onClick = onOnlineInstallClick
            )
            FABEntry(
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp),
                expand = expand,
                enterAnim = fab1Enter,
                exitAnim = fab1Exit,
                text = { Text("本地导入") },
                icon = { Icon(Icons.Filled.Storage) },
                onClick = onLocalInstallClick
            )
            FloatingActionButton(
                modifier = Modifier.padding(top = 16.dp),
                onClick = { onExpandStateChange(!expand) }
            ) {
                val rotate = animate(if (expand) 45f else 0f)
                // Rotate 45° to be Close Icon
                // TODO: 2020/11/13 Use animation icon to instead this.
                Icon(
                    modifier = Modifier.graphicsLayer(rotationZ = rotate),
                    imageVector = Icons.Filled.Add
                )
            }
        }
    }
}

@Composable
private fun FABEntry(
    modifier: Modifier = Modifier,
    expand: Boolean,
    enterAnim: AnimationSpec<Float>,
    exitAnim: AnimationSpec<Float>,
    text: @Composable BoxScope.() -> Unit,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val transition =
        animate(if (expand) 1f else 0f, animSpec = if (expand) enterAnim else exitAnim)

    if (transition > 0f) {
        Row(modifier = modifier.wrapContentSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.graphicsLayer(
                    alpha = transition,
                    scaleY = transition,
                    scaleX = transition
                )
            ) {
                Card(
                    modifier = Modifier.height(40.dp)
                        .padding(vertical = 4.dp, horizontal = 16.dp)
                        .wrapContentWidth(),
                    elevation = 2.dp
                ) {
                    Providers(AmbientContentAlpha provides ContentAlpha.medium) {
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
                    scaleX = transition
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
    Card(modifier = modifier, elevation = 2.dp) {
        Column(Modifier.clickable(onClick = onClick)) {
            // Body
            Row {
                Column(
                    Modifier.weight(1f).padding(16.dp)
                ) {
                    // Title
                    Text(title, style = MaterialTheme.typography.h5)
                    // Description
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = description, style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
                // Actions
                Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                    Row(Modifier.padding(top = 8.dp, end = 4.dp)) {
                        var dropdown by remember { mutableStateOf(false) }
                        DropdownMenu(toggle = {
                            IconButton(onClick = { dropdown = true }) {
                                Icon(imageVector = Icons.Filled.MoreVert)
                            }
                        }, expanded = dropdown, onDismissRequest = { dropdown = false }) {
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
            // Footer
            Row(
                modifier = Modifier.padding(
                    top = 4.dp,
                    bottom = 8.dp,
                    start = 16.dp,
                    end = 8.dp
                )
                    .fillMaxWidth().heightIn(min = 32.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Providers(AmbientContentColor provides MaterialTheme.colors.primary) {
                    AnimatedVisibility(
                        visible = selected,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Icon(Icons.Filled.CheckCircle)
                    }
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                    Providers(AmbientContentAlpha provides ContentAlpha.medium) {
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

    suspend fun showDialog(): DialogResult {
        mutex.withLock {
            return try {
                suspendCancellableCoroutine<DialogResult> { cont ->
                    currentData = Data(cont)
                }
            } finally {
                currentData = null
            }
        }
    }

    class Data(
        private val continuation: CancellableContinuation<DialogResult>
    ) {
        fun confirm() {
            continuation.resume(DialogResult.CONFIRM)
        }

        fun dismiss() {
            continuation.resume(DialogResult.DISMISS)
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
        MyAlertDialog(onDismissRequest = {
            data.dismiss()
        }, title = {
            Text("确认删除吗？")
        }, text = {
            Text("分包内的模组、地图、材质都将一并删除，且无法恢复！请谨慎选择")
        }, dismissButton = {
            TextButton(onClick = {
                data.dismiss()
            }) {
                Text("取消")
            }
        }, confirmButton = {
            TextButton(onClick = {
                data.confirm()
            }) {
                Text("删除")
            }
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
            emptyPage = {
                EmptyPage()
            },
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