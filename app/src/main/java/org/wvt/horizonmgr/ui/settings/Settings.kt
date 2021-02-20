package org.wvt.horizonmgr.ui.settings

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.BuildConfig
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.components.AnimateLogo
import org.wvt.horizonmgr.ui.components.HorizonDivider
import org.wvt.horizonmgr.ui.theme.LocalThemeConfig
import org.wvt.horizonmgr.ui.theme.LocalThemeController

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Settings(
    versionName: String,
    onNavClick: () -> Unit,
    requestCustomTheme: () -> Unit
) {
    val themeController = LocalThemeController.current
    val themeConfig = LocalThemeConfig.current

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = {}, navigationIcon = {
            IconButton(onClick = onNavClick) {
                Icon(Icons.Filled.ArrowBack, "返回")
            }
        }, backgroundColor = Color.Transparent, elevation = 0.dp)
        Row(Modifier.padding(top = 8.dp, start = 54.dp)) {
            AnimateLogo(Modifier.padding(top = 8.dp))
            Column(Modifier.padding(start = 24.dp)) {
                Text(text = "Horizon 管理器", style = MaterialTheme.typography.h5)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("By")
                    Image(
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                        contentDescription = "LOGO",
                        painter = painterResource(
                            id = if (isSystemInDarkTheme()) R.drawable.ic_logo_banner_dark
                            else R.drawable.ic_logo_banner
                        )
                    )
                }
                Providers(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = versionName,
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
        Text(
            modifier = Modifier.padding(top = 48.dp, bottom = 8.dp, start = 72.dp),
            text = "个性化",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )

        var dropdownMenuExpanded by remember { mutableStateOf(false) }

        ListItem(
            modifier = Modifier.clickable(onClick = { dropdownMenuExpanded = true }),
            icon = { Icon(imageVector = Icons.Default.Timelapse, null) },
            text = { Text("夜间模式") },
            secondaryText = {
                Box {
                    Text(if (themeConfig.followSystemDarkMode) "跟随系统" else "自定义")
                    DropdownMenu(
                        expanded = dropdownMenuExpanded,
                        onDismissRequest = { dropdownMenuExpanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            themeController.setFollowSystemDarkTheme(true)
                            dropdownMenuExpanded = false
                        }) { Text("跟随系统") }
                        DropdownMenuItem(onClick = {
                            themeController.setFollowSystemDarkTheme(false)
                            dropdownMenuExpanded = false
                        }) { Text("自定义") }
                    }
                }
            },
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizonDivider(Modifier.height(32.dp).padding(end = 16.dp))
                    Switch(
                        enabled = !themeConfig.followSystemDarkMode, // 自定义时可以修改
                        checked = themeConfig.isDark,
                        onCheckedChange = { themeController.setCustomDarkTheme(it) }
                    )
                }
            },
            singleLineSecondaryText = true
        )

        ListItem(
            modifier = Modifier.clickable(onClick = requestCustomTheme),
            icon = { Icon(imageVector = Icons.Filled.FormatPaint, null) },
            text = { Text("自定义主题色") }
        )

        Text(
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp, start = 72.dp),
            text = "其他",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )

        ListItem(
            modifier = Modifier.clickable(onClick = {}),
            icon = { Icon(imageVector = Icons.Filled.Storage, null) },
            text = { Text("清除下载缓存") }
        )

        ListItem(
            modifier = Modifier.clickable(onClick = {}),
            icon = { Icon(Icons.Filled.PushPin, null) },
            text = { Text("固定文件夹") }
        )
    }
}