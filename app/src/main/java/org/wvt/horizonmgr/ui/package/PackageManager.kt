package org.wvt.horizonmgr.ui.`package`

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.AmbientContentColor
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.components.InputDialogHost
import org.wvt.horizonmgr.ui.components.InputDialogHostState
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.main.SelectedPackageUUIDAmbient
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
    val vm = dependenciesViewModel<PackageManagerViewModel>()
    val selectedPackageUUID = SelectedPackageUUIDAmbient.current
    onCommit(selectedPackageUUID) {
        vm.setSelectedPackage(selectedPackageUUID)
    }

    val vmPackages by vm.packages.collectAsState()
    val vmPs by vm.progressState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val confirmDeleteDialogHostState = remember { ConfirmDeleteDialogHostState() }
    val inputDialogHostState = remember { InputDialogHostState() }

    val context = ContextAmbient.current

    vmPs?.let {
        ProgressDialog(onCloseRequest = vm::dismiss, state = it)
    }

    Column {
        // Top App Bar
        TopAppBar(title = {
            Text("分包管理")
        }, navigationIcon = {
            IconButton(onClick = onNavClick) { Icon(Icons.Filled.Menu) }
        }, actions = {
            // Online Install Button
            IconButton(onClick = { vm.startInstallPackageActivity(context) }) {
                Icon(Icons.Filled.GetApp)
            }
        }, backgroundColor = MaterialTheme.colors.surface)

        if (vmPackages.isNullOrEmpty()) {
            // Tips when there was no packages installed.
            Box(Modifier.fillMaxSize()) {
                Row(Modifier.align(Alignment.Center)) {
                    Text("您还未安装分包，请点击")
                    Icon(Icons.Filled.GetApp)
                    Text("按钮在线安装")
                }
            }
        } else LazyColumnForIndexed(
            modifier = Modifier.fillMaxSize(),
            items = vmPackages,
            contentPadding = PaddingValues(top = 8.dp, bottom = 64.dp)
        ) { index, item ->
            PackageItem(
                modifier = Modifier.padding(16.dp, 8.dp),
                title = item.name,
                description = item.description,
                installTime = item.timeStr,
                selected = item.uuid == selectedPackageUUID,
                onClick = { onPackageSelect(item.uuid) },
                onInfoClick = { vm.showInfo(context, item.uuid) },
                onDeleteClick = { vm.deletePackage(item.uuid, confirmDeleteDialogHostState) },
                onRenameClick = { vm.renamePackage(item.uuid, inputDialogHostState) },
                onCloneClick = { vm.clonePackage(item.uuid, inputDialogHostState) }
            )
        }
    }
    SnackbarHost(hostState = snackbarHostState)
    ConfirmDeleteDialogHost(state = confirmDeleteDialogHostState)
    InputDialogHost(state = inputDialogHostState)
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
    val emphasis = AmbientEmphasisLevels.current
    Card(modifier = modifier.clickable(onClick = onClick), elevation = 2.dp) {
        Column {
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
                ProvideEmphasis(emphasis = emphasis.medium) {
                    Row(Modifier.padding(top = 8.dp, end = 4.dp)) {
                        IconButton(onClick = onInfoClick) {
                            Icon(asset = Icons.Filled.Info)
                        }
                        var dropdown by remember { mutableStateOf(false) }
                        DropdownMenu(toggle = {
                            IconButton(onClick = { dropdown = true }) {
                                Icon(asset = Icons.Filled.MoreVert)
                            }
                        }, expanded = dropdown, onDismissRequest = { dropdown = false }) {
                            DropdownMenuItem(onClick = {
                                dropdown = false
                                onDeleteClick()
                            }) { Text("删除") }
                            DropdownMenuItem(onClick = {
                                dropdown = false
                                onCloneClick()
                            }) { Text("克隆") }
                            DropdownMenuItem(onClick = {
                                dropdown = false
                                onRenameClick()
                            }) { Text("重命名") }
                        }
                    }
                }
            }
            // Footer
            Row(
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp, start = 16.dp, end = 8.dp)
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
                    ProvideEmphasis(emphasis = emphasis.medium) {
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
        AlertDialog(onDismissRequest = {
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