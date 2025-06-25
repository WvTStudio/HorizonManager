package org.wvt.horizonmgr.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.wvt.horizonmgr.ui.joingroup.JoinGroupScreen
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.viewmodel.JoinGroupViewModel
import androidx.core.net.toUri

class JoinGroupActivity : ComponentActivity() {
    private val viewModel: JoinGroupViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidHorizonManagerTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    JoinGroupScreen(
                        viewModel = viewModel,
                        onClose = { finish() },
                        openURL = ::openURL
                    )
                }
            }
        }
    }

    private fun openURL(url: String) {
        try {
            val intent = Intent()
                .setAction(Intent.ACTION_VIEW)
                .setData(url.toUri())
            startActivity(intent)
        } catch (e: Exception) {
            viewModel.startQQFailed()
        }
    }
}