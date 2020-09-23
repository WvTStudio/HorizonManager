package org.wvt.horizonmgr.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class CustomThemeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidHorizonManagerTheme {
                Surface(Modifier.fillMaxSize()) {
                    CustomTheme { finish() }
                }
            }
        }
    }
}