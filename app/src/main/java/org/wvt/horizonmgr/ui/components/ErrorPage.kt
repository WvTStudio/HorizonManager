package org.wvt.horizonmgr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorPage(
    modifier: Modifier = Modifier.fillMaxSize(),
    message: @Composable () -> Unit,
    onRetryClick: () -> Unit
) {
    Column(modifier, Arrangement.Center, Alignment.CenterHorizontally) {
        message()
        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = onRetryClick
        ) {
            Text("重试")
        }
    }
}