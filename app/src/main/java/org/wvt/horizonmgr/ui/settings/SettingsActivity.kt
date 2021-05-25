package org.wvt.horizonmgr.ui.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import dagger.hilt.android.AndroidEntryPoint
import org.wvt.horizonmgr.BuildConfig
import org.wvt.horizonmgr.ui.startActivity
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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