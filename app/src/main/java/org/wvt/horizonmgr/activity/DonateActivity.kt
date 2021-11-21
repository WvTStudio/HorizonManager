package org.wvt.horizonmgr.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.wvt.horizonmgr.ui.donate.DonateScreen
import org.wvt.horizonmgr.ui.theme.AndroidDonateTheme
import org.wvt.horizonmgr.viewmodel.DonateViewModel

@AndroidEntryPoint
class DonateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm by viewModels<DonateViewModel>()
        setContent {
            AndroidDonateTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    DonateScreen(vm = vm, onClose = { finish() })
                }
            }
        }
    }
}