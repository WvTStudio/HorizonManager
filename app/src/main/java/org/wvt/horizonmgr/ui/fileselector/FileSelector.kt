package org.wvt.horizonmgr.ui.fileselector

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastMap
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.viewmodel.FileSelectorViewModel

@Composable
fun FileSelector(
    modifier: Modifier,
    viewModel: FileSelectorViewModel,
    onSelect: (absolutePath: String) -> Unit,
    onClose: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val favoriteFolders by viewModel.favoriteFolders.collectAsState()
    val paths by viewModel.pathTabs.collectAsState()
    val currentPathDepth by viewModel.currentPathDepth.collectAsState()
    val listFiles by viewModel.listFiles.collectAsState()

    DisposableEffect(Unit) {
        viewModel.init()
        onDispose {}
    }

    Column(modifier) {
        CustomAppBar(
            onCancel = onClose,
            data = PathTabData(paths, currentPathDepth),
            onSelectDepth = { viewModel.selectPathDepth(it) }
        )
        Crossfade(state) {
            when (it) {
                FileSelectorViewModel.State.Succeed -> PathList(
                    modifier = Modifier.fillMaxSize(),
                    pinedPath = favoriteFolders.fastMap { PathListEntry.Folder(it) },
                    entries = listFiles,
                    onPinnedFolderSelect = { index -> viewModel.enterFavoriteFolder(index) },
                    onPinnedFolderStar = { index, star -> viewModel.pinnedUnStar(index) },
                    onFolderSelect = { index -> viewModel.enterListFolder(index) },
                    onFileSelect = { index -> onSelect(viewModel.getFileAbsolutePath(index)) },
                    onFolderStar = { index, star ->
                        if (star) {
                            viewModel.star(index)
                        } else {
                            viewModel.unStar(index)
                        }
                    }
                )
                FileSelectorViewModel.State.Error.CannotReadFolder -> ErrorPage(
                    modifier = Modifier.fillMaxSize(),
                    message = { Text("无法读取该文件夹") },
                    onRetryClick = { viewModel.refresh() }
                )
                is FileSelectorViewModel.State.Error.Other -> ErrorPage(
                    modifier = Modifier.fillMaxSize(),
                    message = { Text("出现未知错误，请检查日志文件") },
                    onRetryClick = { viewModel.refresh() }
                )
                FileSelectorViewModel.State.Loading -> Box(
                    Modifier.fillMaxSize(),
                    Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}