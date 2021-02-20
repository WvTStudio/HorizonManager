package org.wvt.horizonmgr.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.setContent
import org.wvt.horizonmgr.BuildConfig
import org.wvt.horizonmgr.ui.startActivity
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val version = "Version " + BuildConfig.VERSION_NAME
        setContent {
            AndroidHorizonManagerTheme {
                Surface {
                    Settings(
                        versionName = version,
                        onNavClick = { finish() },
                        requestCustomTheme = { startActivity<CustomThemeActivity>() }
                    )
                }
            }
        }
    }
}