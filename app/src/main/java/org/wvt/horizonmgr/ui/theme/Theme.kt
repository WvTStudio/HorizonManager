package org.wvt.horizonmgr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.staticAmbientOf
import androidx.compose.ui.Modifier

val AmbientThemeController = staticAmbientOf<ThemeController>()
val AmbientThemeConfig = staticAmbientOf<ThemeConfig>()

interface ThemeController {
    fun setFollowSystemDarkTheme(enable: Boolean)
    fun setCustomDarkTheme(enable: Boolean)
    fun setLightColor(color: Colors)
    fun setDarkColor(color: Colors)
    fun setAppbarAccent(enable: Boolean)
}

@Immutable
data class ThemeConfig(
    val followSystemDarkTheme: Boolean,
    val customDarkTheme: Boolean,
    val lightColor: Colors,
    val darkColor: Colors,
    val appbarAccent: Boolean
)

@Composable
fun HorizonManagerTheme(
    controller: ThemeController = DefaultThemeController,
    config: ThemeConfig = DefaultThemeConfig,
    content: @Composable () -> Unit
) {
    val darkTheme =
        if (config.followSystemDarkTheme) isSystemInDarkTheme()
        else config.customDarkTheme

    val colors = if (darkTheme) config.darkColor else config.lightColor
    Providers(
        AmbientThemeController provides controller,
        AmbientThemeConfig provides config
    ) {
        MaterialTheme(
            colors = colors,
            content = content
        )
    }
}

@Composable
fun PreviewTheme(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    MaterialTheme(colors = DefaultThemeConfig.lightColor) {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colors.background, content = content
        )
    }
}

object DefaultThemeController : ThemeController {
    override fun setFollowSystemDarkTheme(enable: Boolean) {}
    override fun setCustomDarkTheme(enable: Boolean) {}
    override fun setLightColor(color: Colors) {}
    override fun setDarkColor(color: Colors) {}
    override fun setAppbarAccent(enable: Boolean) {}
}

val DefaultThemeConfig = ThemeConfig(
    followSystemDarkTheme = true,
    customDarkTheme = false,
    lightColor = LightColorPalette,
    darkColor = DarkColorPalette,
    appbarAccent = false
)