package org.wvt.horizonmgr.ui.joingroup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class JoinGroupActivity : AppCompatActivity() {
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm = dependenciesViewModel<JoinGroupViewModel>()
            val groups by vm.groups.collectAsState()
            val isLoading by vm.isLoading.collectAsState()
            val loadError by vm.loadError.collectAsState()
            val startError = vm.startQQError.collectAsState().value
            val snackbar = remember { SnackbarHostState() }

            val scope = rememberCoroutineScope()

            LaunchedEffect(startError) {
                if (startError != null) {
                    vm.handledError()
                    scope.launch {
                        snackbar.showSnackbar("启动 QQ 失败")
                    }
                }
            }

            AndroidHorizonManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize()) {
                            TopAppBar(title = {
                                Text("加入群组")
                            }, navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                                }
                            }, backgroundColor = MaterialTheme.colors.surface)
                            when {
                                isLoading && loadError == null -> {
                                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                                !isLoading && loadError == null -> GroupList(groups,
                                    onGroupSelect = {
                                        vm.joinGroup(it.url, this@JoinGroupActivity)
                                    }
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
}