package org.wvt.horizonmgr.ui.onlinemoddetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent
import org.wvt.horizonmgr.ui.components.Background
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class OnlineModDetailActivity : AppCompatActivity() {
    companion object {
        const val INTENT_MOD_ID = "mod_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val modId = intent.getStringExtra(INTENT_MOD_ID) ?: error("Mod id not specified.")

        // TODO: 2020/10/30
        setContent {
            AndroidHorizonManagerTheme {
                Background {
                    OnlineModDetail()
                }
            }
        }
    }
}