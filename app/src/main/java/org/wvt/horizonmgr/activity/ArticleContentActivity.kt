package org.wvt.horizonmgr.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import org.koin.androidx.viewmodel.ext.android.viewModel
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

class ArticleContentActivity : ComponentActivity() {
    private val viewModel by viewModel<ArticleContentViewModel>()

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