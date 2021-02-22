package org.wvt.horizonmgr.ui.community

import android.app.DownloadManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class CommunityActivity : AppCompatActivity() {
    private lateinit var dm: DownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Community(onClose = ::close)
                }
            }
        }
    }

    private fun close() {
        finish()
    }
}