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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.wvt.horizonmgr.ui.HorizonManagerAmbient
import org.wvt.horizonmgr.ui.components.InputDialogHost
import org.wvt.horizonmgr.ui.components.InputDialogHostState
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import org.wvt.horizonmgr.ui.main.DrawerStateAmbient
import org.wvt.horizonmgr.ui.startActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume

private data class PackageManagerItem(
    val uuid: String,
    val name: String,
    val timeStr: String,
    val description: String
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun PackageManager(selectedPackageUUID: String?, onPackageSelect: (uuid: String?) -> Unit) {
    val context = ContextAmbient.current
    val drawerState = DrawerStateAmbient.current
    var packages by remember { mutableStateOf<List<PackageManagerItem>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val confirmDeleteDialogHostState = remember { ConfirmDeleteDialogHostState() }
    val renameDialogHostState = remember { InputDialogHostState() }

    val horizonMgr = HorizonManagerAmbient.current

    var progressDialogState by remember { mutableStateOf<ProgressDialogState?>(null) }

    fun refresh(): Job {
        return scope.launch {
            try {
                val dateFormatter = SimpleDateFormat.getDateInstance()
                packages = horizonMgr.getLocalPackages().map {
                    PackageManagerItem(
                        it.uuid,
                        it.customName,
                        dateFormatter.format(Date(it.installTimeStamp)),
                        "无额外描述"
                    )
                }
            } catch (e: Exception) {
                // TODO Display error message
            }
        }
    }

    onActive {
        val job = refresh()
        onDispose { job.cancel() }
    }

    progressDialogState?.let {
        ProgressDialog(onCloseRequest = { progressDialogState = null }, state = it)
    }

    Column {
        TopAppBar(title = {
            Text("分包管理")
        }, navigationIcon = {
            IconButton(onClick = {
                drawerState.open()
            }) { Icon(Icons.Filled.Menu) }
        }, actions = {
            IconButton(onClick = {
                context.startActivity<InstallPackageActivity>()
            }) { Icon(Icons.Filled.GetApp) }
        }, backgroundColor = MaterialTheme.colors.surface)
        if (packages.isNullOrEmpty()) {
            Stack(Modifier.fillMaxSize()) {
                Row(Modifier.align(Alignment.Center)) {
                    Text("您还未安装分包，请点击")
                    Icon(Icons.Filled.GetApp)
                    Text("按钮在线安装")
                }
            }
        } else LazyColumnForIndexed(
            modifier = Modifier.fillMaxSize(),
            items = packages
        ) { index, item ->
            if (index == 0) Spacer(modifier = Modifier.height(8.dp))
            PackageItem(
                modifier = Modifier.padding(16.dp, 8.dp),
                title = item.name,
                description = item.description,
                installTime = item.timeStr,
                selected = item.uuid == selectedPackageUUID,
                onClick = { onPackageSelect(item.uuid) },
                onInfoClick = { PackageDetailActivity.start(context, item.uuid) },
                onDeleteClick = {
                    scope.launch {
                        if (confirmDeleteDialogHostState.showDialog() ==
                            ConfirmDeleteDialogHostState.DialogResult.CONFIRM
                        ) {
                            progressDialogState = ProgressDialogState.Loading("正在删除")
                            try {
                                horizonMgr.deletePackage(item.uuid)
                            } catch (e: Exception) {
                                progressDialogState =
                                    ProgressDialogState.Failed("删除失败", e.localizedMessage ?: "")
                                return@launch
                            }
                            progressDialogState = ProgressDialogState.Finished("删除成功")
                            refresh()
                        }
                    }
                },
                onRenameClick = {
                    scope.launch {
                        val result =
                            renameDialogHostState.showDialog("重命名", "请输入新名称")
                        if (result is InputDialogHostState.DialogResult.Confirm) {
                            progressDialogState = ProgressDialogState.Loading("正在重命名")
                            try {
                                horizonMgr.renamePackage(item.uuid, result.name)
                            } catch (e: Exception) {
                                progressDialogState =
                                    ProgressDialogState.Failed("重命名失败", e.localizedMessage ?: "")
                                return@launch
                            }
                            progressDialogState = ProgressDialogState.Finished("重命名完成")
                            refresh()
                        }
                    }
                },
                onCloneClick = {
                    scope.launch {
                        val result =
                            renameDialogHostState.showDialog("克隆", "请输入新名称")
                        if (result is InputDialogHostState.DialogResult.Confirm) {
                            progressDialogState = ProgressDialogState.Loading("正在克隆")
                            try {
                                horizonMgr.clonePackage(item.uuid, result.name)
                            } catch (e: Exception) {
                                progressDialogState =
                                    ProgressDialogState.Failed("克隆失败", e.localizedMessage ?: "")
                                return@launch
                            }
                            progressDialogState = ProgressDialogState.Finished("克隆完成")
                            refresh()
                        }
                    }
                }
            )
        }
    }
    SnackbarHost(hostState = snackbarHostState)
    ConfirmDeleteDialogHost(state = confirmDeleteDialogHostState)
    InputDialogHost(state = renameDialogHostState)
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
/*

@Preview
@Composable
fun PackageItemPreview() {
    HorizonManagerComposeTheme {
        Surface {
            PackageItem("Test package",
                "无额外描述",
                "2020-8-8",
                true, {}, {}, {}, {}, {})
        }
    }
}*/
