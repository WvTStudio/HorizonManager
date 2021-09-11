package org.wvt.horizonmgr.ui.pacakgemanager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import dagger.hilt.android.AndroidEntryPoint
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

@AndroidEntryPoint
class PackageDetailActivity : AppCompatActivity() {
    private val viewModel by viewModels<PackageDetailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidHorizonManagerTheme {
                Surface {
                    PackageDetailScreen(viewModel, onCloseClick = ::finish)
                }
            }
        }
    }
}