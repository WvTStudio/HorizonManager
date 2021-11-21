package org.wvt.horizonmgr.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.wvt.horizonmgr.ui.joingroup.JoinGroupScreen
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.viewmodel.JoinGroupViewModel

@AndroidEntryPoint
class JoinGroupActivity : AppCompatActivity() {
    private val viewModel: JoinGroupViewModel by viewModels()

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
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: Exception) {
            viewModel.startQQFailed()
        }
    }
}