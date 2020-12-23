package org.wvt.horizonmgr.ui.theme

import androidx.compose.animation.animate
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
    val followSystemDarkMode: Boolean,
    val isSystemInDark: Boolean,
    val isCustomInDark: Boolean,
    val lightColor: Colors,
    val darkColor: Colors,
    val appbarAccent: Boolean
) {
    val isDark = if (followSystemDarkMode) isSystemInDark else isCustomInDark
}

@Composable
fun HorizonManagerTheme(
    controller: ThemeController = DefaultThemeController,
    config: ThemeConfig = DefaultThemeConfig,
    content: @Composable () -> Unit
) {
    val targetColors = if (config.isDark) config.darkColor else config.lightColor
    val colors = Colors(
        primary = animate(targetColors.primary),
        primaryVariant = animate(targetColors.primaryVariant),
        secondary = animate(targetColors.secondary),
        secondaryVariant = animate(targetColors.secondaryVariant),
        background = animate(targetColors.background),
        surface = animate(targetColors.surface),
        error = animate(targetColors.error),
        onPrimary = animate(targetColors.onPrimary),
        onSecondary = animate(targetColors.onSecondary),
        onBackground = animate(targetColors.onBackground),
        onSurface = animate(targetColors.onSurface),
        onError = animate(targetColors.onError),
        isLight = targetColors.isLight
    )
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
    followSystemDarkMode = true,
    isSystemInDark = false,
    isCustomInDark = false,
    lightColor = LightColorPalette,
    darkColor = DarkColorPalette,
    appbarAccent = false
)