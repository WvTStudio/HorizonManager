package org.wvt.horizonmgr.ui.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class CustomThemeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContent {
            AndroidHorizonManagerTheme {
                Surface(Modifier.fillMaxSize()) {
                    CustomThemeScreen { finish() }
                }
            }
        }
    }
}