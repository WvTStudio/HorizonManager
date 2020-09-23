package org.wvt.horizonmgr.ui.joingroup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Providers
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.ui.AndroidDependenciesProvider
import org.wvt.horizonmgr.ui.WebAPIAmbient
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class JoinGroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            AndroidDependenciesProvider {
                AndroidHorizonManagerTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        JoinGroup(
                            onClose = { finish() },
                            onGroupSelect = {
                                scope.launch {
                                    try {
                                        joinGroup(it.url)
                                    } catch (e: Throwable) {
                                        // TODO 添加提示信息
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private suspend fun joinGroup(intentUrl: String) = withContext(Dispatchers.Main) {
        val intent = Intent()
        intent.data = Uri.parse(intentUrl)
        startActivity(intent)
    }
}