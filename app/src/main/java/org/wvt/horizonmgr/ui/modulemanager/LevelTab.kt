package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.ui.AmbientHorizonManager
import org.wvt.horizonmgr.ui.components.InputDialogHost
import org.wvt.horizonmgr.ui.components.InputDialogHostState
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import org.wvt.horizonmgr.ui.main.AmbientSelectedPackageUUID

enum class LevelTabType {
    MC, IC
}

@Composable
internal fun LevelTab(type: LevelTabType) {
    val pkgId = AmbientSelectedPackageUUID.current
    val horizonMgr = AmbientHorizonManager.current
    var items by remember { mutableStateOf<List<HorizonManager.LevelInfo>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var progressDialogState by remember { mutableStateOf<ProgressDialogState?>(null) }
    val inputDialogState = remember { InputDialogHostState() }
    fun load() {
        if (pkgId == null) return
        scope.launch {
            try {
                if (type == LevelTabType.IC) items = horizonMgr.getICLevels(pkgId)
                else if (type == LevelTabType.MC) items = horizonMgr.getMCLevels()
            } catch (e: Exception) {
                // TODO 显示错误信息
                e.printStackTrace()
            }
        }
    }
    onActive {
        load()
    }

    if (pkgId == null) return
    LazyColumn {
        itemsIndexed(items = items) { index, item ->
            LevelItem(
                modifier = Modifier.padding(16.dp),
                title = item.name,
                screenshot = item.screenshot,
                onRenameClicked = {
                    scope.launch {
                        val result: InputDialogHostState.DialogResult =
                            inputDialogState.showDialog("请输入新名称", "新名称")
                        if (result is InputDialogHostState.DialogResult.Confirm) {
                            progressDialogState = ProgressDialogState.Loading("正在重命名")
                            try {
                                horizonMgr.renameLevelNameByPath(item.path, result.name)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                progressDialogState =
                                    ProgressDialogState.Failed("重命名失败", e.localizedMessage)
                                return@launch
                            }
                            progressDialogState = ProgressDialogState.Finished("重命名成功")
                            load()
                        }
                    }
                },
                onDeleteClicked = {
                    scope.launch {
                        progressDialogState = ProgressDialogState.Loading("正在删除")
                        try {
                            horizonMgr.deleteLevelByPath(item.path)
                        } catch (e: Exception) {
                            progressDialogState =
                                ProgressDialogState.Failed("删除失败", e.localizedMessage ?: "")
                            return@launch
                        }
                        progressDialogState = ProgressDialogState.Finished("删除完成")
                        load()
                    }
                }
            )
            if (index == items.size - 1) Spacer(modifier = Modifier.size(64.dp))
        }
    }

    InputDialogHost(state = inputDialogState)

    progressDialogState?.let {
        ProgressDialog(onCloseRequest = { progressDialogState = null }, state = it)
    }
}