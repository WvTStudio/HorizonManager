package org.wvt.horizonmgr.ui.fileselector

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed

sealed class PathListEntry {
    @Immutable
    data class Folder(val name: String) : PathListEntry()

    @Immutable
    data class File(val name: String) : PathListEntry()
}

@Composable
internal fun PathList(
    pinedPath: List<PathListEntry.Folder>,
    entries: List<PathListEntry>,
    onPinnedFolderSelect: (index: Int) -> Unit,
    onPinnedFolderStar: (index: Int, star: Boolean) -> Unit,
    onFolderSelect: (index: Int) -> Unit,
    onFileSelect: (index: Int) -> Unit,
    onFolderStar: (index: Int, star: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val header = @Composable {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "已固定",
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.subtitle1
        )
        Column(Modifier.animateContentSize()) {
            pinedPath.fastForEachIndexed { index, folder ->
                FolderItem(name = folder.name,
                    onClick = { onPinnedFolderSelect(index) },
                    isStared = true,
                    onStarChange = { onPinnedFolderStar(index, false) }
                )
            }
        }
        Divider(Modifier.padding(top = 8.dp, bottom = 8.dp))
    }

    Crossfade(entries) { entries ->
        if (entries.isEmpty()) {
            // 如果子文件是空的，那么 LazyColumnForIndex 不会触发，需要手动检测
            Column(modifier) {
                header()
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "空文件夹"
                    )
                }
            }
        } else {
            LazyColumn(modifier) {
                item { header() }
                itemsIndexed(entries) { index, item ->
                    if (item is PathListEntry.Folder) {
                        FolderItem(
                            name = item.name,
                            onClick = { onFolderSelect(index) },
                            isStared = false,
                            onStarChange = { onFolderStar(index, it) }
                        )
                    } else if (item is PathListEntry.File) {
                        FileItem(name = item.name, onClick = { onFileSelect(index) })
                    }
                }
            }
        }
    }
}
