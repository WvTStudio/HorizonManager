package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.ui.components.*

@Composable
internal fun ICLevelTab(
    viewModel: ICLevelTabViewModel
) {
    val levels by viewModel.levels.collectAsState()
    val state by viewModel.state.collectAsState()

    val scope = rememberCoroutineScope()
    var progressDialogState by remember { mutableStateOf<ProgressDialogState?>(null) }
    val inputDialogState = remember { InputDialogHostState() }

    DisposableEffect(Unit) {
        viewModel.load()
        onDispose {  }
    }

    Crossfade(state) {
        when (it) {
            ICLevelTabViewModel.State.Loading -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
            ICLevelTabViewModel.State.PackageNotSelected -> {
                EmptyPage(Modifier.fillMaxSize()) {
                    Text("你还没有选择分包")
                }
            }
            ICLevelTabViewModel.State.OK -> {
                if (levels.isEmpty()) {
                    EmptyPage(Modifier.fillMaxSize()) {
                        Text("当前还没有地图")
                    }
                } else LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 64.dp)
                ) {
                    itemsIndexed(items = levels) { _, item ->
                        LevelItem(
                            modifier = Modifier.padding(16.dp),
                            title = item.name,
                            screenshot = item.screenshot,
                            onRenameClicked = {
                                scope.launch {
                                    val result: InputDialogHostState.DialogResult =
                                        inputDialogState.showDialog(
                                            "New-${item.name}",
                                            "请输入新名称",
                                            "新名称"
                                        )
                                    if (result is InputDialogHostState.DialogResult.Confirm) {
                                        progressDialogState = ProgressDialogState.Loading("正在重命名")
                                        try {
                                            viewModel.renameLevel(item, result.input)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            progressDialogState =
                                                ProgressDialogState.Failed(
                                                    "重命名失败",
                                                    e.localizedMessage ?: ""
                                                )
                                            return@launch
                                        }
                                        progressDialogState = ProgressDialogState.Finished("重命名成功")
                                        viewModel.load()
                                    }
                                }
                            },
                            onDeleteClicked = {
                                scope.launch {
                                    progressDialogState = ProgressDialogState.Loading("正在删除")
                                    try {
                                        viewModel.deleteLevel(item)
                                    } catch (e: Exception) {
                                        progressDialogState =
                                            ProgressDialogState.Failed(
                                                "删除失败",
                                                e.localizedMessage ?: ""
                                            )
                                        return@launch
                                    }
                                    progressDialogState = ProgressDialogState.Finished("删除完成")
                                    viewModel.load()
                                }
                            }
                        )
                    }
                }
            }
            is ICLevelTabViewModel.State.Error -> ErrorPage(
                modifier = Modifier.fillMaxSize(),
                message = { Text("出现错误") },
                onRetryClick = { viewModel.load() }
            )
        }
    }


    InputDialogHost(state = inputDialogState)

    progressDialogState?.let {
        ProgressDialog(onCloseRequest = { progressDialogState = null }, state = it)
    }
}