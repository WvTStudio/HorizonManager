package org.wvt.horizonmgr.ui.joingroup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.wvt.horizonmgr.HorizonManagerApplication
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor
import org.wvt.horizonmgr.ui.theme.SideEffectStatusBar

class JoinGroupActivity : AppCompatActivity() {
    private val vm by viewModels<JoinGroupViewModel> {
        (application as HorizonManagerApplication).dependenciesVMFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.refresh()

        setContent {
            val groups by vm.groups.collectAsState()
            val isLoading by vm.isLoading.collectAsState()
            val loadError by vm.loadError.collectAsState()
            val startError by vm.startQQError.collectAsState()
            val snackbar = remember { SnackbarHostState() }

            LaunchedEffect(startError) {
                if (startError) {
                    try {
                        snackbar.showSnackbar("启动 QQ 失败")
                    } finally {
                        vm.handledError()
                    }
                }
            }

            AndroidHorizonManagerTheme {
                SideEffectStatusBar()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize()) {
                            TopAppBar(
                                title = { Text("加入群组") },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                                    }
                                },
                                backgroundColor = AppBarBackgroundColor
                            )
                            when {
                                isLoading && loadError == null -> {
                                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                                !isLoading && loadError == null -> GroupList(groups,
                                    onGroupSelect = { openURL(it.url) }
                                )
                                !isLoading && loadError != null -> {
                                    ErrorPage(
                                        modifier = Modifier.fillMaxSize(),
                                        message = { Text("加载失败") },
                                        onRetryClick = { vm.refresh() }
                                    )
                                }
                            }
                        }
                        SnackbarHost(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            hostState = snackbar
                        )
                    }
                }
            }
        }
    }

    private fun openURL(url: String) {
        try {
            val intent = Intent()
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: Exception) {
            vm.startQQFailed()
        }
    }
}