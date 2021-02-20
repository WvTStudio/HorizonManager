package org.wvt.horizonmgr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorPage(
    modifier: Modifier = Modifier,
    message: @Composable () -> Unit,
    onRetryClick: () -> Unit
) {
    Column(modifier, Arrangement.Center, Alignment.CenterHorizontally) {
        Providers(LocalContentAlpha provides ContentAlpha.medium) {
            message()
        }
        OutlinedButton(
            modifier = Modifier.padding(top = 16.dp),
            onClick = onRetryClick) {
            Icon(imageVector = Icons.Filled.Refresh, contentDescription = "重试")
        }
    }
}