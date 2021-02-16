package org.wvt.horizonmgr.ui.news

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.DisposableEffect
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
            DisposableEffect(vm) {
                vm.load(id)
                onDispose {
                    // TODO: 2021/2/7 添加 Dispose 逻辑
                }
            }
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    NewsContent(vm, ::finish)
                }
            }
        }
    }
}