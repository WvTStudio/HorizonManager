package org.wvt.horizonmgr.ui.pacakgemanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import dagger.hilt.android.AndroidEntryPoint
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

@AndroidEntryPoint
class PackageDetailActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context, pkgId: String) {
            val intent = Intent(context, PackageDetailActivity::class.java).also {
                it.putExtra("package_uuid", pkgId)
            }
            context.startActivity(intent)
        }
    }

    private val viewModel by viewModels<PackageDetailViewModel>() /*{
        (application as HorizonManagerApplication).dependenciesVMFactory
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val packageUUID = intent.getStringExtra("package_uuid") ?: return finish()
        viewModel.setPackageUUID(packageUUID)
        viewModel.load()

        setContent {
            AndroidHorizonManagerTheme {
                Surface {
                    PackageDetailScreen(viewModel, onCloseClick = ::finish)
                }
            }
        }
    }
}