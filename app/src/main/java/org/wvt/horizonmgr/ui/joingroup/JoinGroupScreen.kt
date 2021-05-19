package org.wvt.horizonmgr.ui.joingroup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor

@Composable
fun JoinGroupScreen(
    onClose: () -> Unit,
    openURL: (String) -> Unit,
    vm: JoinGroupViewModel = hiltViewModel()
) {

    val groups by vm.groups.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val loadError by vm.loadError.collectAsState()
    val startError by vm.startQQError.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(vm) { vm.refresh() }
    
    LaunchedEffect(startError) {
        if (startError) {
            try {
                snackbar.showSnackbar("启动 QQ 失败")
            } finally {
                vm.handledError()
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("加入群组") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
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