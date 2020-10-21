package org.wvt.horizonmgr.ui.`package`

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.compose.ui.platform.setContent
import org.wvt.horizonmgr.ui.AndroidDependenciesProvider
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val packageUUID = intent.getStringExtra("package_uuid") ?: return finish()

        setContent {
            AndroidDependenciesProvider {
                AndroidHorizonManagerTheme {
                    Surface {
                        PackageInfo(packageUUID)
                    }
                }
            }
        }
    }
}