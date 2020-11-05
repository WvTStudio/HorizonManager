package org.wvt.horizonmgr.ui.pacakgemanager

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.ui.HorizonManagerAmbient
import org.wvt.horizonmgr.utils.calcSize
import java.io.File

private data class Item(
    val icon: VectorAsset,
    val title: String,
    val content: String
)

private data class Section(
    val title: String,
    val items: List<Item>
)

@Composable
fun PackageInfo(pkgId: String) {
    val context = ContextAmbient.current as AppCompatActivity
    val scope = rememberCoroutineScope()
    var pkgInfo by remember { mutableStateOf<HorizonManager.LocalPackage?>(null) }
    var manifest by remember { mutableStateOf<HorizonManager.PackageManifest?>(null) }
    val horizonMgr = HorizonManagerAmbient.current

    onActive {
        scope.launch {
            try {
                val result = horizonMgr.getPackageInfo(pkgId)!!
                manifest = horizonMgr.parsePackageManifest(result.manifest)
                pkgInfo = result
            } catch (e: Exception) {
            }
        }
    }

    Crossfade(current = pkgInfo) { pkgInfo ->
        if (pkgInfo == null) {
            Stack(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        } else {
            Column {
                TopAppBar(title = {
                    Text(pkgInfo.customName)
                }, navigationIcon = {
                    IconButton(onClick = {
                        context.finish()
                    }) {
                        Icon(Icons.Filled.ArrowBack)
                    }
                }, backgroundColor = MaterialTheme.colors.surface)
                ScrollableColumn {
                    manifest?.let {
                        ManifestSection(manifest = it)
                    }
                    pkgInfo.let {
                        FileSection(pkgInfo = it)
                    }
                }
            }
        }
    }
}

@Composable
private fun ManifestSection(manifest: HorizonManager.PackageManifest) {
    Text(
        text = "清单信息",
        style = MaterialTheme.typography.subtitle2,
        color = MaterialTheme.colors.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 72.dp)
    )
    ListItem(icon = {
        Icon(Icons.Filled.Gamepad, modifier = Modifier.padding(top = 4.dp))
    }, text = {
        Text("游戏版本")
    }, secondaryText = {
        Text(manifest.gameVersion)
    })
    ListItem(icon = {
        Icon(Icons.Filled.Extension, modifier = Modifier.padding(top = 4.dp))
    }, text = {
        Text("分包名称")
    }, secondaryText = {
        Text(manifest.packName)
    })
    ListItem(icon = {
        Icon(Icons.Filled.Description, modifier = Modifier.padding(top = 4.dp))
    }, text = {
        Text("版本信息")
    }, secondaryText = {
        Text(manifest.packVersionName)
    })
    ListItem(icon = {
        Icon(Icons.Filled.Description, modifier = Modifier.padding(top = 4.dp))
    }, text = {
        Text("版本号")
    }, secondaryText = {
        Text(manifest.packVersionCode.toString())
    })
    ListItem(icon = {
        Icon(Icons.Filled.Person, modifier = Modifier.padding(top = 4.dp))
    }, text = {
        Text("开发者")
    }, secondaryText = {
        Text(manifest.developer)
    })
    ListItem(icon = {
        Icon(Icons.Filled.Notes, modifier = Modifier.padding(top = 4.dp))
    }, text = {
        Text("分包描述")
    }, secondaryText = {
        Text(manifest.description)
    })
}

@Composable
private fun FileSection(pkgInfo: HorizonManager.LocalPackage) {
    var size by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    onCommit(pkgInfo) {
        scope.launch {
            val (count, totalSize) = try {
                File(pkgInfo.path).also { println(it) }.calcSize().also { println(it) }
            } catch (e: Exception) {
                e.printStackTrace()
                size = "计算出错"
                return@launch
            }
            size = when {
                totalSize >= 1024 * 1024 -> "${(totalSize / 1024 / 1024)} MB"
                totalSize >= 1024 -> "${(totalSize / 1024)} KB"
                totalSize >= 0 -> "$totalSize B"
                else -> "错误"
            } + "  共 $count 个文件"
        }
    }
    Text(
        text = "文件信息",
        style = MaterialTheme.typography.subtitle2,
        color = MaterialTheme.colors.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 72.dp)
    )
    ListItem(icon = {
        Icon(Icons.Filled.Folder, modifier = Modifier.padding(top = 4.dp))
    }, text = {
        Text("分包路径")
    }, secondaryText = {
        Text(pkgInfo.path)
    })
    ListItem(icon = {
        Icon(Icons.Filled.Details, modifier = Modifier.padding(top = 4.dp))
    }, text = {
        Text("分包大小")
    }, secondaryText = {
        Text(text = if (size == null) "正在计算" else size!!)
    })
}