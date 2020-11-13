package org.wvt.horizonmgr.ui.downloaded

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.components.ProgressDialog
import org.wvt.horizonmgr.ui.main.SelectedPackageUUIDAmbient

@Composable
fun DownloadedMods(onNavClicked: () -> Unit) {
    val vm = dependenciesViewModel<DMViewModel>()
    val mods by vm.mods.collectAsState()
    val progressState by vm.progressState.collectAsState()
    val selected = SelectedPackageUUIDAmbient.current

    onCommit(selected) {
        vm.setSelectedPackage(selected)
        vm.refresh()
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier.zIndex(4.dp.value), title = {
                Text("本地资源")
            }, navigationIcon = {
                IconButton(onClick = onNavClicked, icon = {
                    Icon(Icons.Filled.Menu)
                })
            }, backgroundColor = MaterialTheme.colors.surface
        )

        if (mods.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(16.dp), Alignment.Center) {
                Text("本地资源列表为空，您可以从在线资源中下载到本地")
            }
        } else {
            LazyColumnFor(
                items = mods,
                contentPadding = PaddingValues(top = 8.dp, bottom = 64.dp)
            ) {
                ModItem(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    name = it.name,
                    description = it.description,
                    icon = emptyContent(),
                    onInstallClicked = { vm.install(it) },
                    onDeleteClicked = { vm.delete(it) }
                )
            }
        }
    }

    progressState?.let {
        ProgressDialog(onCloseRequest = vm::dismiss, state = it)
    }
}

@Composable
private fun ModItem(
    modifier: Modifier = Modifier,
    name: String,
    description: String,
    icon: @Composable () -> Unit,
    onDeleteClicked: () -> Unit,
    onInstallClicked: () -> Unit,
) {
    Card(modifier = modifier, elevation = 2.dp) {
        Column {
            Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
                // Name
                Text(text = name, style = MaterialTheme.typography.h6)
                // Description
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = description
                )
            }
            Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
                // Delete Button
                TextButton(
                    modifier = Modifier.padding(start = 16.dp).align(Alignment.CenterStart),
                    onClick = onDeleteClicked
                ) { Text("删除", color = MaterialTheme.colors.primary) }
                // Install Button
                IconButton(
                    modifier = Modifier.padding(end = 16.dp).align(Alignment.CenterEnd),
                    onClick = onInstallClicked
                ) { Icon(asset = Icons.Filled.Extension, tint = MaterialTheme.colors.primary) }
            }
        }
    }
}

private sealed class DownloadItemState {
    object Finished : DownloadItemState()
    class Error(val message: Exception) : DownloadItemState()
    class Downloading(
        val name: String,
        val progress: Float
    ) : DownloadItemState()
}