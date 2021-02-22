package org.wvt.horizonmgr.ui.pacakgemanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.compose.ui.platform.setContent
import org.wvt.horizonmgr.HorizonManagerApplication
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class PackageDetailActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context, pkgId: String) {
            val intent = Intent(context, PackageDetailActivity::class.java).also {
                it.putExtra("package_uuid", pkgId)
            }
            context.startActivity(intent)
        }
    }

    private val viewModel by viewModels<PackageDetailViewModel> {
        (application as HorizonManagerApplication).dependenciesVMFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val packageUUID = intent.getStringExtra("package_uuid") ?: return finish()
        viewModel.setPackageUUID(packageUUID)
        viewModel.load()

        setContent {
            AndroidHorizonManagerTheme {
                Surface {
                    PackageInfo(viewModel, onCloseClick = ::finish)
                }
            }
        }
    }
}