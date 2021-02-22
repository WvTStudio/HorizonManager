package org.wvt.horizonmgr.ui.pacakgemanager

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.components.ErrorPage

@Composable
fun PackageInfo(
    vm: PackageDetailViewModel,
    onCloseClick: () -> Unit
) {
    val info by vm.info.collectAsState()
    val pkgSize by vm.pkgSize.collectAsState()
    val state = vm.state.collectAsState().value

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = {
            Text("分包详情")
        }, navigationIcon = {
            IconButton(onClick = onCloseClick) {
                Icon(Icons.Filled.ArrowBack, "返回")
            }
        }, backgroundColor = MaterialTheme.colors.surface)
        Crossfade(
            modifier = Modifier.weight(1f),
            targetState = state
        ) { state ->
            when (state) {
                PackageDetailViewModel.State.LOADING -> {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
                PackageDetailViewModel.State.FAILED -> {
                    ErrorPage(message = { Text("获取分包详情失败") }, onRetryClick = { vm.load() })
                }
                PackageDetailViewModel.State.SUCCEED -> {
                    val info = info
                    if (info != null) {
                        LazyColumn {
                            item {
                                ManifestSection(info = info)
                                FileSection(path = info.installDir, packSize = remember(pkgSize) {
                                    when (val pkgSize = pkgSize) {
                                        is PackageDetailViewModel.PackageSize.Succeed -> pkgSize.sizeStr + "  共 ${pkgSize.count} 个文件"
                                        is PackageDetailViewModel.PackageSize.Failed -> "计算出错"
                                        is PackageDetailViewModel.PackageSize.Loading -> "正在计算"
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ManifestSection(
    info: PackageDetailViewModel.PackageInformation,
) {
    Text(
        text = "清单信息",
        style = MaterialTheme.typography.subtitle2,
        color = MaterialTheme.colors.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 72.dp)
    )
    ListItem(icon = {
        Icon(Icons.Filled.Extension, modifier = Modifier.padding(top = 4.dp), contentDescription = null)
    }, text = {
        Text("分包名称")
    }, secondaryText = {
        Text(info.packageName)
    })
    ListItem(icon = {
        Icon(Icons.Filled.Person, modifier = Modifier.padding(top = 4.dp), contentDescription = null)
    }, text = {
        Text("开发者")
    }, secondaryText = {
        Text(info.developer)
    })
    ListItem(icon = {
        Icon(Icons.Filled.Description, modifier = Modifier.padding(top = 4.dp), contentDescription = null)
    }, text = {
        Text("版本信息")
    }, secondaryText = {
        Text(info.version)
    })
    ListItem(icon = {
        Icon(Icons.Filled.Description, modifier = Modifier.padding(top = 4.dp), contentDescription = null)
    }, text = {
        Text("版本号")
    }, secondaryText = {
        Text(info.versionCode)
    })
    ListItem(icon = {
        Icon(Icons.Filled.Description, modifier = Modifier.padding(top = 4.dp), contentDescription = null)
    }, text = {
        Text("UUID")
    }, secondaryText = {
        Text(info.installUUID)
    })
    ListItem(icon = {
        Icon(Icons.Filled.Description, modifier = Modifier.padding(top = 4.dp), contentDescription = null)
    }, text = {
        Text("分包 UUID")
    }, secondaryText = {
        Text(info.packageUUID)
    })
    ListItem(icon = {
        Icon(Icons.Filled.Gamepad, modifier = Modifier.padding(top = 4.dp), contentDescription = null)
    }, text = {
        Text("游戏版本")
    }, secondaryText = {
        Text(info.gameVersion)
    })
    ListItem(icon = {
        Icon(Icons.Filled.Notes, modifier = Modifier.padding(top = 4.dp), contentDescription = null)
    }, text = {
        Text("分包描述")
    }, secondaryText = {
        Text(info.description)
    })
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FileSection(
    path: String,
    packSize: String
) {
    Text(
        text = "文件信息",
        style = MaterialTheme.typography.subtitle2,
        color = MaterialTheme.colors.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 72.dp)
    )
    ListItem(icon = {
        Icon(Icons.Filled.Folder, modifier = Modifier.padding(top = 4.dp), contentDescription = null)
    }, text = {
        Text("分包路径")
    }, secondaryText = {
        Text(path)
    })
    ListItem(icon = {
        Icon(Icons.Filled.Storage, modifier = Modifier.padding(top = 4.dp), contentDescription = null)
    }, text = {
        Text("分包大小")
    }, secondaryText = {
        Text(text = packSize)
    })
}