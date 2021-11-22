package org.wvt.horizonmgr.ui.joingroup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor
import org.wvt.horizonmgr.viewmodel.JoinGroupViewModel

@Composable
fun JoinGroupScreen(
    onClose: () -> Unit,
    openURL: (String) -> Unit,
    viewModel: JoinGroupViewModel = hiltViewModel()
) {

    val groups by viewModel.groups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val startError by viewModel.startQQError.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) { viewModel.refresh() }

    val errorText = stringResource(R.string.group_screen_tip_qq_failed)
    LaunchedEffect(startError) {
        if (startError) {
            try {
                snackbar.showSnackbar(errorText)
            } finally {
                viewModel.handledError()
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(stringResource(R.string.group_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            stringResource(R.string.navigation_action_back)
                        )
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
                        message = { Text(stringResource(R.string.group_screen_tip_load_failed)) },
                        onRetryClick = { viewModel.refresh() }
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