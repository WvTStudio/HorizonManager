package org.wvt.horizonmgr.ui.joingroup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

@AndroidEntryPoint
class JoinGroupActivity : AppCompatActivity() {

    private val vm: JoinGroupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    JoinGroupScreen(
                        viewModel = hiltViewModel(),
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
            vm.startQQFailed()
        }
    }
}