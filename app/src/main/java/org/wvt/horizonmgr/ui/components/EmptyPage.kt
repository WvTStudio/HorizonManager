package org.wvt.horizonmgr.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun EmptyPage(
    modifier: Modifier = Modifier,
    message: @Composable () -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        message()
    }
}