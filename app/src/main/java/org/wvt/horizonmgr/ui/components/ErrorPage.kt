package org.wvt.horizonmgr.ui.components

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ErrorPage(
    modifier: Modifier = Modifier.fillMaxSize(),
    message: @Composable () -> Unit,
    onRetryClick: () -> Unit
) {
    Column(modifier, Arrangement.Center, Alignment.CenterHorizontally) {
        message()
        Button(onClick = onRetryClick) {
            Text("重试")
        }
    }
}