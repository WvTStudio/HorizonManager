package org.wvt.horizonmgr.ui.theme

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit


private val config = mutableStateOf<ThemeConfig>(DefaultThemeConfig)

private fun setLightStatusBar(window: Window) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val wic = window.decorView.windowInsetsController!!
        wic.setSystemBarsAppearance(
            APPEARANCE_LIGHT_STATUS_BARS,
            APPEARANCE_LIGHT_STATUS_BARS
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // SET
        val flag = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = flag or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

private fun clearLightStatusBar(window: Window) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val wic = window.decorView.windowInsetsController!!
        wic.setSystemBarsAppearance(0, APPEARANCE_LIGHT_STATUS_BARS)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // UNSET
        val flag = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = flag and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }

}

/**
 * 该组件使用 SharedPreference 实现主题配置的持久化存储
 */
@Composable
fun AndroidHorizonManagerTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val theController = remember(context) { AndroidThemeController(context) }
    val activity = (LocalContext.current as Activity)

    DisposableEffect(config.value) {
        val config = config.value
        val color = if (config.isDark) config.darkColor else config.lightColor

        val statusBarLight: Boolean
        val statusBarColor: Color

        if (config.isDark) {
            statusBarLight = false
            statusBarColor = Color(0xFF121212)
        } else {
            if (config.appbarAccent) {
                statusBarColor = config.lightColor.primaryVariant
                val lightText = color.onPrimary == Color.White
                statusBarLight = !lightText
            } else {
                statusBarColor = config.lightColor.surface
                val lightText = color.onSurface == Color.White
                statusBarLight = !lightText
            }
        }

        activity.window.statusBarColor = statusBarColor.toArgb()
        if (statusBarLight) {
            setLightStatusBar(activity.window)
        } else {
            clearLightStatusBar(activity.window)
        }

        onDispose { }
    }

    DisposableEffect(isSystemInDarkTheme()) {
        theController.update()
        onDispose { }
    }

    HorizonManagerTheme(
        controller = theController,
        config = config.value,
        content = content
    )
}

private class LocalConfig(context: Context) {
    private val themePreference =
        context.getSharedPreferences("theme", Context.MODE_PRIVATE)
    private val lightThemePreference =
        context.getSharedPreferences("light_theme", Context.MODE_PRIVATE)
    private val darkThemePreference =
        context.getSharedPreferences("dark_theme", Context.MODE_PRIVATE)

    fun getFollowSystemDarkMode(): Boolean {
        return themePreference.getBoolean("follow_system_dark_theme", true)
    }

    fun setFollowSystemDarkMode(enable: Boolean) {
        themePreference.edit { putBoolean("follow_system_dark_theme", enable) }
    }

    fun getCustomDarkMode(): Boolean {
        return themePreference.getBoolean("custom_dark_theme", false)
    }

    fun setCustomDarkMode(enable: Boolean) {
        themePreference.edit { putBoolean("custom_dark_theme", enable) }
    }

    fun getLightColor(): Colors {
        return with(lightThemePreference) {
            try {
                lightColors(
                    primary = getString("primary", null)!!.toColor(),
                    primaryVariant = getString("primary_variant", null)!!.toColor(),
                    onPrimary = getString("on_primary", null)!!.toColor(),
                    secondary = getString("secondary", null)!!.toColor(),
                    secondaryVariant = getString("secondary_variant", null)!!.toColor(),
                    onSecondary = getString("on_secondary", null)!!.toColor()
                )
            } catch (e: Exception) {
                LightColorPalette
            }
        }
    }

    fun setLightColor(color: Colors) {
        lightThemePreference.edit {
            putString("primary", color.primary.toHexString())
            putString("primary_variant", color.primaryVariant.toHexString())
            putString("on_primary", color.onPrimary.toHexString())
            putString("secondary", color.secondary.toHexString())
            putString("secondary_variant", color.secondaryVariant.toHexString())
            putString("on_secondary", color.onSecondary.toHexString())
        }
    }

    fun getDarkColor(): Colors {
        return with(darkThemePreference) {
            try {
                darkColors(
                    primary = getString("primary", null)!!.toColor(),
                    primaryVariant = getString("primary_variant", null)!!.toColor(),
                    onPrimary = getString("on_primary", null)!!.toColor(),
                    secondary = getString("secondary", null)!!.toColor(),
                    onSecondary = getString("on_secondary", null)!!.toColor()
                )
            } catch (e: Exception) {
                DarkColorPalette
            }
        }
    }

    fun setDarkColor(color: Colors) {
        darkThemePreference.edit {
            putString("primary", color.primary.toHexString())
            putString("primary_variant", color.primaryVariant.toHexString())
            putString("on_primary", color.onPrimary.toHexString())
            putString("secondary", color.secondary.toHexString())
            putString("on_secondary", color.onSecondary.toHexString())
        }
    }

    fun isAppbarAccent(): Boolean {
        return themePreference.getBoolean("is_appbar_accent", false)
    }

    fun setAppbarAccent(enable: Boolean) {
        themePreference.edit {
            putBoolean("is_appbar_accent", enable)
        }
    }
}

internal class AndroidThemeController(
    private val context: Context
) : ThemeController {

    private val localConfig = LocalConfig(context)

    init {
        update()
    }

    fun update() {
        val lightColor = localConfig.getLightColor()
        val darkColor = localConfig.getDarkColor()
        val appbarAccent = localConfig.isAppbarAccent()
//        val followSystem = AppCompatDelegate.getDefaultNightMode()

        val configFollowSystem = localConfig.getFollowSystemDarkMode()
        val isConfigCustomInDark = localConfig.getCustomDarkMode()
        val isSystemInDark =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        config.value = config.value.copy(
            followSystemDarkMode = configFollowSystem,
            isSystemInDark = isSystemInDark,
            isCustomInDark = isConfigCustomInDark,
            lightColor = lightColor,
            darkColor = darkColor,
            appbarAccent = appbarAccent
        )
    }

    fun isFollowingSystemDarkTheme(): Boolean {
        return localConfig.getFollowSystemDarkMode()
    }

    override fun setFollowSystemDarkTheme(enable: Boolean) {
        localConfig.setFollowSystemDarkMode(enable)
        update()
    }

    override fun setCustomDarkTheme(enable: Boolean) {
        localConfig.setCustomDarkMode(enable)
        update()
    }

    override fun setLightColor(color: Colors) {
        localConfig.setLightColor(color)
        update()
    }

    override fun setDarkColor(color: Colors) {
        localConfig.setDarkColor(color)
        update()
    }

    override fun setAppbarAccent(enable: Boolean) {
        localConfig.setAppbarAccent(enable)
        update()
    }
}

private fun String.toColor(): Color {
    return Color(toLong(16))
}

private fun Color.toHexString(): String {
    return value.shr(32).toString(16)
}