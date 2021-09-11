package org.wvt.horizonmgr.ui.fileselector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

sealed class FileSelectorResult {
    object Canceled : FileSelectorResult()
    data class Succeed(val filePath: String) : FileSelectorResult()
}

class FileSelectorResultContract : ActivityResultContract<Context, FileSelectorResult>() {
    override fun createIntent(context: Context, input: Context): Intent {
        return Intent(input, FileSelectorActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): FileSelectorResult {
        when (resultCode) {
            FileSelectorActivity.SELECTED -> {
                if (intent == null) return FileSelectorResult.Canceled
                return with(intent) {
                    FileSelectorResult.Succeed(
                        getStringExtra(FileSelectorActivity.FILE_PATH)
                            ?: error("The key 'file_path' was not specified.")
                    )
                }
            }
            FileSelectorActivity.CANCEL -> return FileSelectorResult.Canceled
            else -> return FileSelectorResult.Canceled
        }
    }
}


@AndroidEntryPoint
class FileSelectorActivity : AppCompatActivity() {
    companion object {
        const val CANCEL = 0
        const val SELECTED = 1
        const val FILE_PATH = "file_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = SharedFileChooserViewModel
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    FileSelector(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = hiltViewModel(),
                        onSelect = {
                            viewModel.setSelected(it)
                            onFileSelect(it)
                                   },
                        onClose = { onCancel() }
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
        val intent = Intent().apply { putExtra("file_path", filePath) }
        setResult(SELECTED, intent)
        finish()
    }
}