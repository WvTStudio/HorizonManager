package org.wvt.horizonmgr.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.wvt.horizonmgr.ui.donate.DonateScreen
import org.wvt.horizonmgr.ui.theme.AndroidDonateTheme
import org.wvt.horizonmgr.viewmodel.DonateViewModel

class DonateActivity : ComponentActivity() {
    val vm by viewModel<DonateViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidDonateTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    DonateScreen(vm = vm, onClose = { finish() })
                }
            }
        }
    }
}