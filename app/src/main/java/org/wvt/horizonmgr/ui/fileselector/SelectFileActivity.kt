package org.wvt.horizonmgr.ui.fileselector

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.compose.ui.platform.setContent
import kotlinx.coroutines.suspendCancellableCoroutine
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.ui.theme.HorizonManagerTheme
import kotlin.coroutines.resume

class SelectFileActivity : AppCompatActivity() {
    companion object {
        const val CANCEL = 0
        const val SELECTED = 1

        suspend fun startForResult(context: ComponentActivity): String? {
            return suspendCancellableCoroutine { cont ->
                context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (it.resultCode == SELECTED) {
                        if (cont.isActive) cont.resume(it.data!!.getStringExtra("file_path"))
                    } else {
                        if (cont.isActive) cont.resume(null)
                    }
                }.launch(Intent(context, SelectFileActivity::class.java))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidHorizonManagerTheme {
                Surface {
                    FileSelector(
                        onCancel = ::onCancel,
                        onSelect = ::onFileSelect
                    )
                }
            }
        }
    }

    private fun onCancel() {
        setResult(CANCEL)
        finish()
    }

    private fun onFileSelect(filePath: String) {
        val intent = Intent().apply {
            putExtra("file_path", filePath)
        }
        setResult(SELECTED, intent)
        finish()
    }
}