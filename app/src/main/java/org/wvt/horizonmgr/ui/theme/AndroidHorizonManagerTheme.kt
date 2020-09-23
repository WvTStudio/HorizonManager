package org.wvt.horizonmgr.ui.theme

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.core.content.edit

private var config = mutableStateOf<ThemeConfig>(DefaultThemeConfig)
private var controller: ThemeController? = null

/**
 * 该组件使用 SharedPreference 实现主题配置的持久化存储
 */
@Composable
fun AndroidHorizonManagerTheme(content: @Composable () -> Unit) {
    val context = ContextAmbient.current

    val theController = remember(context) {
        controller ?: ControllerImpl(context).also { controller = it }
    }

    HorizonManagerTheme(
        controller = theController,
        config = config.value,
        content = content
    )
}

private class ControllerImpl(context: Context) : ThemeController {
    private val themePreference =
        context.getSharedPreferences("theme", Context.MODE_PRIVATE)
    private val lightThemePreference =
        context.getSharedPreferences("light_theme", Context.MODE_PRIVATE)
    private val darkThemePreference =
        context.getSharedPreferences("dark_theme", Context.MODE_PRIVATE)

    init {
        val lightColor = with(lightThemePreference) {
            try {
                lightColors(
                    primary = getString("primary", null)!!.toColor(),
                    primaryVariant = getString("primary_variant", null)!!.toColor(),
                    onPrimary = getString("on_primary", null)!!.toColor(),
                    secondary = getString("secondary", null)!!.toColor(),
                    secondaryVariant = getString("secondary_variant", null)!!.toColor(),
                    onSecondary = getString("on_secondary", null)!!.toColor()
                )
            } catch (e: Exception) { LightColorPalette }
        }
        val darkColor = with(darkThemePreference) {
            try {
                darkColors(
                    primary = getString("primary", null)!!.toColor(),
                    primaryVariant = getString("primary_variant", null)!!.toColor(),
                    onPrimary = getString("on_primary", null)!!.toColor(),
                    secondary = getString("secondary", null)!!.toColor(),
                    onSecondary = getString("on_secondary", null)!!.toColor()
                )
            } catch (e: Exception) { DarkColorPalette }
        }
        config.value = config.value.copy(
            followSystemDarkTheme = themePreference.getBoolean(
                "follow_system_dark_theme", true
            ),
            customDarkTheme = themePreference.getBoolean("custom_dark_theme", false),
            lightColor = lightColor,
            darkColor = darkColor
        )
    }


    override fun setFollowSystemDarkTheme(enable: Boolean) {
        themePreference.edit { putBoolean("follow_system_dark_theme", enable) }
        config.value = config.value.copy(followSystemDarkTheme = enable)
    }

    override fun setCustomDarkTheme(enable: Boolean) {
        themePreference.edit { putBoolean("custom_dark_theme", enable) }
        config.value = config.value.copy(customDarkTheme = enable)
    }

    override fun setLightColor(color: Colors) {
        lightThemePreference.edit {
            putString("primary", color.primary.toHexString())
            putString("primary_variant", color.primaryVariant.toHexString())
            putString("on_primary", color.onPrimary.toHexString())
            putString("secondary", color.secondary.toHexString())
            putString("secondary_variant", color.secondaryVariant.toHexString())
            putString("on_secondary", color.onSecondary.toHexString())
        }
        config.value = config.value.copy(lightColor = color)
    }

    override fun setDarkColor(color: Colors) {
        darkThemePreference.edit {
            putString("primary", color.primary.toHexString())
            putString("primary_variant", color.primaryVariant.toHexString())
            putString("on_primary", color.onPrimary.toHexString())
            putString("secondary", color.secondary.toHexString())
            putString("on_secondary", color.onSecondary.toHexString())
        }
        config.value = config.value.copy(darkColor = color)
    }

    override fun setAppbarAccent(enable: Boolean) {
        themePreference.edit { putBoolean("appbar_accent", enable) }
        config.value = config.value.copy(appbarAccent = enable)
    }
}

private fun String.toColor(): Color {
    return Color(toLong(16))
}

private fun Color.toHexString(): String {
    return value.shr(32).toString(16)
}