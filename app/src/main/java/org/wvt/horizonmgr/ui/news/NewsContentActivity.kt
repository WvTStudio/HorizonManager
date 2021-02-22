package org.wvt.horizonmgr.ui.news

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import org.wvt.horizonmgr.HorizonManagerApplication
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class NewsContentActivity : AppCompatActivity() {

    private val vm by viewModels<NewsContentViewModel> {
        (application as HorizonManagerApplication).dependenciesVMFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra("id", -1)
        if (id == -1) error("Id not specified")

        vm.load(id)

        setContent {
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    NewsContent(vm, ::finish)
                }
            }
        }
    }
}