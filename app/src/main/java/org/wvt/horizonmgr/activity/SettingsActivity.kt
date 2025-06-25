package org.wvt.horizonmgr.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.material.Surface
import androidx.core.view.WindowCompat
import org.wvt.horizonmgr.BuildConfig
import org.wvt.horizonmgr.ui.settings.SettingsScreen
import org.wvt.horizonmgr.ui.startActivity
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val version = "Version " + BuildConfig.VERSION_NAME

        setContent {
            AndroidHorizonManagerTheme(fullScreen = true) {
                Surface {
                    SettingsScreen(
                        versionName = version,
                        onBackClick = { finish() },
                        requestCustomTheme = { startActivity<CustomThemeActivity>() }
                    )
                }
            }
        }
    }
}