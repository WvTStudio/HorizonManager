package org.wvt.horizonmgr.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun Background(
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable () -> Unit
) {
    Surface(modifier = modifier, content = content, color = MaterialTheme.colors.background)
}