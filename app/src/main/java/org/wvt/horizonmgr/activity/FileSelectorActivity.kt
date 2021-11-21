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
import org.wvt.horizonmgr.ui.fileselector.FileSelector
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.viewmodel.FileSelectorViewModel
import org.wvt.horizonmgr.viewmodel.SharedFileChooserViewModel

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

    private val sharedViewModel = SharedFileChooserViewModel

    private val viewModel by viewModels<FileSelectorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidHorizonManagerTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    FileSelector(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = viewModel,
                        onSelect = {
                            sharedViewModel.setSelected(it)
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