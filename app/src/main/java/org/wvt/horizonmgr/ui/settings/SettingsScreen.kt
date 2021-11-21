package org.wvt.horizonmgr.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FormatPaint
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.components.AnimateLogo
import org.wvt.horizonmgr.ui.components.HorizonDivider
import org.wvt.horizonmgr.ui.theme.LocalThemeConfig
import org.wvt.horizonmgr.ui.theme.LocalThemeController

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsScreen(
    versionName: String,
    onBackClick: () -> Unit,
    requestCustomTheme: () -> Unit
) {
    val themeController = LocalThemeController.current
    val themeConfig = LocalThemeConfig.current

    var followSystemDarkMode by remember { mutableStateOf(themeConfig.followSystemDarkMode) }
    var customIsDark by remember { mutableStateOf(themeConfig.isCustomInDark) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = {
            Text(stringResource(id = R.string.settings_screen_appbar_title))
        }, navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    stringResource(id = R.string.settings_screen_appbar_back_desc)
                )
            }
        }, backgroundColor = Color.Transparent, elevation = 0.dp)
        Row(Modifier.padding(top = 24.dp, start = 54.dp)) {
            AnimateLogo(Modifier.padding(top = 8.dp))
            Column(Modifier.padding(start = 32.dp)) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.h5
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("By")
                    Image(
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                        contentDescription = "LOGO",
                        painter = painterResource(
                            id = if (themeConfig.isDark) R.drawable.ic_logo_banner_dark
                            else R.drawable.ic_logo_banner
                        )
                    )
                }
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
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
            text = stringResource(id = R.string.settings_screen_divider_customization),
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )

        var dropdownMenuExpanded by remember { mutableStateOf(false) }

        ListItem(
            modifier = Modifier.clickable(onClick = { dropdownMenuExpanded = true }),
            icon = { Icon(imageVector = Icons.Rounded.Timelapse, null) },
            text = { Text(stringResource(id = R.string.settings_screen_option_darkmode_label)) },
            secondaryText = {
                Box {
                    Text(
                        if (followSystemDarkMode) stringResource(id = R.string.settings_screen_option_darkmode_follow_system)
                        else stringResource(id = R.string.settings_screen_option_darkmode_custom)
                    )
                    DropdownMenu(
                        expanded = dropdownMenuExpanded,
                        onDismissRequest = { dropdownMenuExpanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            followSystemDarkMode = true
                            themeController.setFollowSystemDarkTheme(true)
                            dropdownMenuExpanded = false
                        }) { Text(stringResource(id = R.string.settings_screen_option_darkmode_follow_system)) }
                        DropdownMenuItem(onClick = {
                            followSystemDarkMode = false
                            themeController.setFollowSystemDarkTheme(false)
                            dropdownMenuExpanded = false
                        }) { Text(stringResource(id = R.string.settings_screen_option_darkmode_custom)) }
                    }
                }
            },
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizonDivider(
                        Modifier
                            .height(32.dp)
                            .padding(end = 16.dp)
                    )
                    Switch(
                        enabled = !followSystemDarkMode, // 自定义时可以修改
                        checked = customIsDark,
                        onCheckedChange = {
                            customIsDark = it
                            themeController.setCustomDarkTheme(it)
                        }
                    )
                }
            },
            singleLineSecondaryText = true
        )

        ListItem(
            modifier = Modifier.clickable(onClick = requestCustomTheme),
            icon = { Icon(imageVector = Icons.Rounded.FormatPaint, null) },
            text = { Text(stringResource(id = R.string.settings_screen_option_custom_theme)) }
        )

/*        Text(
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp, start = 72.dp),
            text = "其他",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )

        ListItem(
            modifier = Modifier.clickable(onClick = {}),
            icon = { Icon(imageVector = Icons.Rounded.Storage, null) },
            text = { Text("清除下载缓存") }
        )

        ListItem(
            modifier = Modifier.clickable(onClick = {}),
            icon = { Icon(Icons.Rounded.PushPin, null) },
            text = { Text("固定文件夹") }
        )*/
    }
}