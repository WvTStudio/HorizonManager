package org.wvt.horizonmgr.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import org.wvt.horizonmgr.ui.community.CommunityScreen
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.viewmodel.CommunityViewModel
import org.wvt.horizonmgr.viewmodel.ContextFactory

@AndroidEntryPoint
class CommunityActivity : AppCompatActivity() {
    private val viewModel by viewModels<CommunityViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return modelClass.getConstructor(ContextFactory::class.java).newInstance(
                    ContextFactory {
                        this@CommunityActivity
                    })
            }
        }
    }

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