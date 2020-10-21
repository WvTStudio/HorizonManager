package org.wvt.horizonmgr.ui.settings

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import org.wvt.horizonmgr.BuildConfig
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.components.AnimateLogo
import org.wvt.horizonmgr.ui.components.HorizonDivider
import org.wvt.horizonmgr.ui.theme.ThemeConfigAmbient
import org.wvt.horizonmgr.ui.theme.ThemeControllerAmbient

@Composable
fun Settings(
    requestCustomTheme: () -> Unit
) {
    val context = ContextAmbient.current as AppCompatActivity
    val versionName = remember { "Version " + BuildConfig.VERSION_NAME }
    val themeController = ThemeControllerAmbient.current
    val themeConfig = ThemeConfigAmbient.current

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = {}, navigationIcon = {
            IconButton(onClick = { context.finish() }) {
                Icon(Icons.Filled.ArrowBack)
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
                        asset = vectorResource(
                            id = if (isSystemInDarkTheme()) R.drawable.ic_logo_banner_dark
                            else R.drawable.ic_logo_banner
                        )
                    )
                }
                val emphasis = AmbientEmphasisLevels.current
                ProvideEmphasis(emphasis = emphasis.medium) {
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
/*        ListItem(
            modifier = Modifier.clickable(onClick = {}),
            icon = { Icon(asset = Icons.Filled.Description) },
            text = { Text("固定文件夹") }
        )*/
        // FIXME 支持关闭跟随系统
        var dropdownMenuExpanded by remember { mutableStateOf(false) }

        DropdownMenu(toggle = {
            ListItem(
                modifier = Modifier.clickable(onClick = { dropdownMenuExpanded = true }),
                icon = { Icon(asset = Icons.Default.Timelapse) },
                text = { Text("夜间模式") },
                secondaryText = {
                    Crossfade(current = themeConfig.followSystemDarkTheme) {
                        Text(if (it) "跟随系统" else "自定义")
                    }
                },
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HorizonDivider(Modifier.height(32.dp).padding(end = 16.dp))
                        Switch(
                            enabled = !themeConfig.followSystemDarkTheme, // 自定义时可以修改
                            checked = if (themeConfig.followSystemDarkTheme) isSystemInDarkTheme()
                            else themeConfig.customDarkTheme,
                            onCheckedChange = {
                                themeController.setCustomDarkTheme(it)
                            })
                    }
                },
                singleLineSecondaryText = true
            )
        }, expanded = dropdownMenuExpanded, onDismissRequest = { dropdownMenuExpanded = false }) {
            DropdownMenuItem(onClick = {
                themeController.setFollowSystemDarkTheme(true)
                dropdownMenuExpanded = false
            }) { Text("跟随系统") }
            DropdownMenuItem(onClick = {
                themeController.setFollowSystemDarkTheme(false)
                dropdownMenuExpanded = false
            }) { Text("自定义") }
        }
        ListItem(
            modifier = Modifier.clickable(onClick = requestCustomTheme),
            icon = { Icon(asset = Icons.Filled.FormatPaint) },
            text = { Text("自定义主题色") }
        )
    }
}

@Preview
@Composable
fun SettingsPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colors.background) {
            Settings(requestCustomTheme = {})
        }
    }
}