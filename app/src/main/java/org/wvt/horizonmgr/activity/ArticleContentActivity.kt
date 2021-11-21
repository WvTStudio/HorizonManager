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
import org.wvt.horizonmgr.ui.article.ArticleContentScreen
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.viewmodel.ArticleContentViewModel

class ArticleContentActivityContract : ActivityResultContract<String, Unit>() {
    override fun createIntent(context: Context, input: String): Intent {
        return Intent(context, ArticleContentActivity::class.java).apply {
            putExtra("id", input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?) {
        return
    }
}

@AndroidEntryPoint
class ArticleContentActivity : AppCompatActivity() {
    private val viewModel by viewModels<ArticleContentViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidHorizonManagerTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    ArticleContentScreen(viewModel, ::finish)
                }
            }
        }
    }
}