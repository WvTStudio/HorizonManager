package org.wvt.horizonmgr.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.wvt.horizonmgr.ui.onlineinstall.OnlineInstallScreen
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.viewmodel.InstallPackageViewModel

class InstallPackageResultContract : ActivityResultContract<Context, Boolean>() {
    companion object {
        const val CANCEL = 0
        const val SUCCEED = 1
    }

    override fun createIntent(context: Context, input: Context): Intent {
        return Intent(context, InstallPackageActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return when (resultCode) {
            SUCCEED -> true
            CANCEL -> false
            else -> false
        }
    }
}

@AndroidEntryPoint
class InstallPackageActivity : AppCompatActivity() {
    private val viewModel by viewModels<InstallPackageViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidHorizonManagerTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    OnlineInstallScreen(
                        viewModel = viewModel,
                        onCancel = { finishWithCancel() },
                        onSucceed = { finishWithSucceed() }
                    )
                }
            }
        }
    }

    private fun finishWithCancel() {
        setResult(InstallPackageResultContract.CANCEL)
        finish()
    }

    private fun finishWithSucceed() {
        setResult(InstallPackageResultContract.SUCCEED)
        finish()
    }
}