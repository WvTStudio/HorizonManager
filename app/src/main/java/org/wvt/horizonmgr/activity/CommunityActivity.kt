package org.wvt.horizonmgr.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.wvt.horizonmgr.ui.community.CommunityScreen
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.viewmodel.CommunityViewModel

class CommunityActivity : ComponentActivity() {
    private val viewModel by viewModel<CommunityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidHorizonManagerTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    CommunityScreen(vm = viewModel, onClose = ::close)
                }
            }
        }
    }

    private fun close() {
        finish()
    }
}