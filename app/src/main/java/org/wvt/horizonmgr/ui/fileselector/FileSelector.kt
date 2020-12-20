package org.wvt.horizonmgr.ui.fileselector

import android.os.Environment
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animate
import androidx.compose.foundation.ScrollableRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.ui.theme.PreviewTheme
import java.io.File
import java.util.*

// TODO: 2020/11/13 重做该组件

private suspend fun getRootStorage(): FileItem {
    return withContext(Dispatchers.IO) {
        val file = Environment.getExternalStorageDirectory()!!
        val parts = file.absolutePath.substringAfter(File.separator).split(File.separator)
        FileItem(
            isDirectory = true,
            name = file.name,
            absolutePath = file.absolutePath,
            parts = parts,
            depth = parts.size
        )
    }
}

private suspend fun getFileItem(path: String): FileItem {
    return withContext(Dispatchers.IO) {
        val file = File(path)
        val parts = file.absolutePath.substringAfter(File.separator).split(File.separator)
        FileItem(
            isDirectory = file.isDirectory,
            name = file.name,
            absolutePath = file.absolutePath,
            parts = parts,
            depth = parts.size
        )
    }
}

private suspend fun getFileItems(path: String): List<FileItem> {
    return withContext(Dispatchers.IO) {
        val locale = Locale.getDefault()
        val file = File(path)
        val tree = file.listFiles() ?: emptyArray()

        withContext(Dispatchers.Default) {
            val fileItems = tree.map {
                val parts = it.absolutePath.substringAfter(File.separator).split(File.separator)
                FileItem(
                    isDirectory = it.isDirectory,
                    name = it.name,
                    absolutePath = it.absolutePath,
                    parts = parts,
                    depth = parts.size
                )
            }
            fileItems.asSequence().sortedBy { it.name.toLowerCase(locale) }
                .sortedByDescending { it.isDirectory }
                .toList()
        }
    }
}

private suspend fun FileItem.resolve(path: String): FileItem {
    return getFileItem(this.absolutePath + File.separator + path)
}

private data class FileItem(
    val isDirectory: Boolean,
    val name: String,
    val absolutePath: String,
    val parts: List<String>,
    val depth: Int
)

@Composable
fun FileSelector(onCancel: () -> Unit, onSelect: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    // 代表了内部存储的路径，例如 "/storage/emulated/0/"
    var storagePath by remember { mutableStateOf<FileItem?>(null) }
    // 代表内部存储的实际深度，例如 "/storage/emulated/0/"，则 storage 为 0，"0" 为 2
    var storageDepth by remember { mutableStateOf(0) }

    // 本状态代表当前持有的路径
    // 本路径只有在子文件夹中选择了新文件夹才会更改，如果只是在 Tab 中更改了 Depth，本路径不会更改
    // 本状态为了实现在不选择新文件夹前可以自由在各个深度中导航
    var currentHolderPath by remember { mutableStateOf<FileItem?>(null) }

    // 当前目录在 currentHolderPath 中的位置，即深度，该深度是子文件夹实际显示的路径
    // 该 depth 从内部存储根目录 "/storage/emulated/0/" 开始计算，起始值为 0
    var currentDepth by remember { mutableStateOf(0) }

    // 代表固定的文件夹
    var fixedPaths by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    // 代表子文件夹列表
    var subPaths by remember { mutableStateOf<List<FileItem>>(emptyList()) }

    // 在 PathTab 中显示的路径，根目录为内部存储根目录，以 "内部存储" 呈现
    var tabPaths by remember { mutableStateOf<List<String>>(listOf("内部存储")) }
    // 在 PathTab 中显示的深度，深度 0 为内部存储根目录
    var tabDepth by remember { mutableStateOf(0) }

    // 初始化任务，获取内置存储的位置，解析该目录的子目录文件，获取固定目录
    onActive {
        isLoading = true
        val job = scope.launch(Dispatchers.Default) {
            val storage = getRootStorage()
            val theFixedPaths = listOf(
                storage.resolve("Download").copy(name = "下载的文件"),
                storage.resolve("Tencent/QQfile_recv").copy(name = "QQ接收的文件")
            )
            storagePath = storage
            storageDepth = storage.depth

            currentHolderPath = storage
            currentDepth = storage.depth

            fixedPaths = theFixedPaths
            subPaths = getFileItems(storage.absolutePath)

            tabPaths = listOf("内部存储")
            tabDepth = 0

            isLoading = false
        }
        onDispose { job.cancel() }
    }

    fun changeDepth(depth: Int) {
        scope.launch(Dispatchers.Default) {
            val actualPath = currentHolderPath!!.parts.joinToString(
                prefix = File.separator,
                separator = File.separator,
                limit = depth,
                truncated = ""
            )
            val subItems = getFileItems(actualPath)
            tabDepth = depth - storageDepth
            subPaths = subItems
            isLoading = false
        }
    }

    fun changeHolderPath(path: FileItem) {
        scope.launch(Dispatchers.Default) {
            try {
                val fileItem = getFileItem(path.absolutePath)
                val subItems = getFileItems(fileItem.absolutePath)
                val theTabPath = mutableListOf<String>("内部存储")
                if (fileItem.depth > storageDepth) {
                    // 从根目录起始到路径结束的名字
                    for (i in storageDepth until fileItem.parts.size) {
                        theTabPath.add(fileItem.parts[i])
                    }
                }
                currentHolderPath = fileItem
                currentDepth = fileItem.depth
                tabPaths = theTabPath
                tabDepth = fileItem.depth - storageDepth
                subPaths = subItems
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    Column {
        CustomAppBar(
            onCancel = onCancel,
            data = PathTabData(tabPaths, tabDepth),
            onSelectDepth = {
                changeDepth(it + storageDepth)
            }
        )
        Crossfade(current = subPaths) { subPaths ->
            PathList(
                modifier = Modifier.fillMaxSize(),
                pinedPath = fixedPaths.fastMap { PathListEntry.Folder(it.name) },
                entries = subPaths.fastMap {
                    if (it.isDirectory) PathListEntry.Folder(it.name)
                    else PathListEntry.File(it.name)
                },
                onPinnedFolderSelect = {
                    changeHolderPath(fixedPaths[it])
                },
                onFolderSelect = {
                    changeHolderPath(subPaths[it])
                },
                onFileSelect = {
                    onSelect(subPaths[it].absolutePath)
                }
            )
        }
    }
}


@Composable
private fun CustomAppBar(
    onCancel: () -> Unit,
    data: PathTabData,
    onSelectDepth: (depth: Int) -> Unit
) {
    Surface(
        modifier = Modifier.zIndex(4.dp.value),
        elevation = 4.dp
    ) {
        Column {
            TopAppBar(
                title = { Text("选择文件") },
                navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.Filled.ArrowBack) } },
                backgroundColor = Color.Transparent,
                elevation = 0.dp
            )
            // 路径指示器
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent) {
                PathTab(data, onSelectDepth)
            }
        }
    }
}

@Immutable
data class PathTabData(
    val paths: List<String>,
    val depth: Int
)

@Composable
private fun PathTab(
    data: PathTabData,
    onSelectDepth: (depth: Int) -> Unit
) {
    val scrollState = rememberScrollState()

    // 每一段路径的宽度
    val sizes = remember { mutableStateListOf<Int>() }

    /*onCommit(data.paths) {
        sizes.clear()
    }*/

    onCommit(data.depth) {
        if (sizes.isNotEmpty()) {
            var value = 0
            for (i in 0..data.depth) {
                value += sizes.getOrNull(i) ?: 0
            }
            value -= sizes.getOrNull(data.depth) ?: 0 / 2
            scrollState.smoothScrollTo(value.toFloat())
        }
    }

    ScrollableRow(
        verticalAlignment = Alignment.CenterVertically,
        scrollState = scrollState
    ) {
        // heading padding
        Spacer(modifier = Modifier.width(72.dp))
        data.paths.forEachIndexed { index, pathName ->
            Box(
                modifier = Modifier.clickable(
                    onClick = { onSelectDepth(index) },
                    indication = null
                ).onGloballyPositioned { sizes.add(index, it.size.width) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                    text = pathName,
                    color = animate(
                        if (data.depth == index) MaterialTheme.colors.onSurface
                        else MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    ) // 选中高亮
                )
            }

            // paths.size = 1:
            //     index: 0 (>)
            //     size:  1 (>)
            // paths.size = 4:
            //     index: 0 > 1 > 2 > 3 (>)
            //     size:  4 > 4 > 4 > 4 (>)
            if (index + 1 < data.paths.size) {
                Icon(
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                    imageVector = Icons.Filled.ChevronRight,
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        // trailing padding
        Spacer(modifier = Modifier.width(32.dp))
    }
}

private sealed class PathListEntry {
    @Immutable
    data class Folder(val name: String) : PathListEntry()

    @Immutable
    data class File(val name: String) : PathListEntry()
}

@Composable
private fun PathList(
    pinedPath: List<PathListEntry.Folder>,
    entries: List<PathListEntry>,
    onPinnedFolderSelect: (index: Int) -> Unit,
    onFolderSelect: (index: Int) -> Unit,
    onFileSelect: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val header = @Composable {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "已固定",
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.subtitle1
        )
        pinedPath.fastForEachIndexed { index, folder ->
            FolderEntry(name = folder.name,
                onClick = { onPinnedFolderSelect(index) },
                isStared = true,
                onStarChange = {}
            )
        }
        Divider(Modifier.padding(top = 8.dp, bottom = 8.dp))
    }

    if (entries.isEmpty()) {
        // 如果子文件是空的，那么 LazyColumnForIndex 不会触发，需要手动检测
        Column(modifier) {
            header()
            Box(Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "空文件夹"
                )
            }
        }
    } else {
        LazyColumn(modifier) {
            itemsIndexed(entries) { index, item ->
                if (index == 0) header()
                if (item is PathListEntry.Folder) {
                    FolderEntry(name = item.name, onClick = { onFolderSelect(index) },
                        isStared = false, onStarChange = {})
                } else if (item is PathListEntry.File) {
                    FileEntry(name = item.name, onClick = { onFileSelect(index) })
                }
            }
        }
    }
}

@Preview
@Composable
private fun CustomAppBarPreview() {
    PreviewTheme {
        CustomAppBar(
            onCancel = {},
            data = PathTabData(listOf("内部存储", "Android"), 0),
            onSelectDepth = {}
        )
    }
}

@Preview
@Composable
private fun PathListPreview() {
    PreviewTheme {
        PathList(
            modifier = Modifier.fillMaxSize(),
            pinedPath = listOf(PathListEntry.Folder("QQ 文件"), PathListEntry.Folder("下载文件")),
            entries = listOf(
                PathListEntry.Folder("Android"),
                PathListEntry.Folder("Download"),
                PathListEntry.Folder("Tencent"),
                PathListEntry.File(".temp"),
            ),
            onPinnedFolderSelect = {},
            onFolderSelect = {},
            onFileSelect = {}
        )
    }
}