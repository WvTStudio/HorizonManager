package org.wvt.horizonmgr.ui.news

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.onCommit
import androidx.compose.ui.platform.setContent
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class NewsContentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra("id", -1)
        if (id == -1) error("Id not specified")

        setContent {
            val vm = dependenciesViewModel<NewsContentViewModel>()
            onCommit(vm) {
                vm.refresh(id)
            }
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    NewsContent(vm, ::finish)
                }
            }
        }
    }
}