package org.wvt.horizonmgr.ui.joingroup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class JoinGroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm = dependenciesViewModel<JoinGroupViewModel>()
            AndroidHorizonManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    JoinGroup(
                        vm,
                        onClose = { finish() },
                        onGroupSelect = {
                            vm.joinGroup(it.url, this)
                        }
                    )
                }
            }
        }
    }
}