package org.wvt.horizonmgr.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

val LocalThemeController =
    staticCompositionLocalOf<ThemeController> { error("No theme controller provided") }
val LocalThemeConfig = staticCompositionLocalOf<ThemeConfig> { error("No theme config provided") }

interface ThemeController {
    fun setFollowSystemDarkTheme(enable: Boolean)
    fun setCustomDarkTheme(enable: Boolean)
    fun setLightColor(color: Colors)
    fun setDarkColor(color: Colors)
    fun setAppbarAccent(enable: Boolean)
}

val AppBarBackgroundColor: Color
    @Composable
    get() {
        val config = LocalThemeConfig.current
        return if (config.appbarAccent && !config.isDark) {
            MaterialTheme.colors.primary
        } else {
            MaterialTheme.colors.surface
        }
    }

val defaultTypography = Typography(
    defaultFontFamily = FontFamily.Default
)

@Stable
class ThemeConfig(
    followSystemDarkMode: Boolean,
    isSystemInDark: Boolean,
    isCustomInDark: Boolean,
    lightColor: Colors,
    darkColor: Colors,
    appbarAccent: Boolean
) {
    var followSystemDarkMode by mutableStateOf(followSystemDarkMode)
        internal set
    var isSystemInDark by mutableStateOf(isSystemInDark)
        internal set
    var isCustomInDark by mutableStateOf(isCustomInDark)
        internal set
    var lightColor by mutableStateOf(lightColor)
        internal set
    var darkColor by mutableStateOf(darkColor)
        internal set
    var appbarAccent by mutableStateOf(appbarAccent)
        internal set

    val isDark: Boolean
        @Composable get() = rememberUpdatedState(if (followSystemDarkMode) isSystemInDark else isCustomInDark).value
    val color: Colors
        @Composable get() = rememberUpdatedState(if (isDark) darkColor else lightColor).value
    val appbarColor: Color
        @Composable get() = rememberUpdatedState(if (appbarAccent && !isDark) color.primary else color.surface).value
    val statusBarColor: Color
        @Composable get() = rememberUpdatedState(if (appbarAccent && !isDark) color.primaryVariant else color.surface).value
}

@Composable
fun HorizonManagerTheme(
    controller: ThemeController = DefaultThemeController,
    config: ThemeConfig = DefaultThemeConfig2,
    content: @Composable () -> Unit
) {
    val targetColors = config.color
    val colors = Colors(
        primary = animateColorAsState(targetColors.primary).value,
        primaryVariant = animateColorAsState(targetColors.primaryVariant).value,
        secondary = animateColorAsState(targetColors.secondary).value,
        secondaryVariant = animateColorAsState(targetColors.secondaryVariant).value,
        background = animateColorAsState(targetColors.background).value,
        surface = animateColorAsState(targetColors.surface).value,
        error = animateColorAsState(targetColors.error).value,
        onPrimary = animateColorAsState(targetColors.onPrimary).value,
        onSecondary = animateColorAsState(targetColors.onSecondary).value,
        onBackground = animateColorAsState(targetColors.onBackground).value,
        onSurface = animateColorAsState(targetColors.onSurface).value,
        onError = animateColorAsState(targetColors.onError).value,
        isLight = targetColors.isLight
    )
    CompositionLocalProvider(
        LocalThemeController provides controller,
        LocalThemeConfig provides config
    ) {
        MaterialTheme(
            colors = colors,
            content = content,
            typography = defaultTypography
        )
    }
}

@Composable
fun PreviewTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalThemeController provides DefaultThemeController,
        LocalThemeConfig provides DefaultThemeConfig2
    ) {
        MaterialTheme(colors = LightColorPalette, content = content, typography = defaultTypography)
    }
}

object DefaultThemeController : ThemeController {
    override fun setFollowSystemDarkTheme(enable: Boolean) {}
    override fun setCustomDarkTheme(enable: Boolean) {}
    override fun setLightColor(color: Colors) {}
    override fun setDarkColor(color: Colors) {}
    override fun setAppbarAccent(enable: Boolean) {}
}

val DefaultThemeConfig2 = ThemeConfig(
    followSystemDarkMode = true,
    isSystemInDark = false,
    isCustomInDark = false,
    lightColor = LightColorPalette,
    darkColor = DarkColorPalette,
    appbarAccent = false
)