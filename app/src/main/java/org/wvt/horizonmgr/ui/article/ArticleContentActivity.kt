package org.wvt.horizonmgr.ui.article

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import org.wvt.horizonmgr.HorizonManagerApplication
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

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

class ArticleContentActivity : AppCompatActivity() {
    private val vm by viewModels<ArticleContentViewModel> {
        (application as HorizonManagerApplication).dependenciesVMFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra("id") ?: error("Id not specified")
        setContent {
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    ArticleContent(id, vm, ::finish)
                }
            }
        }
    }
}