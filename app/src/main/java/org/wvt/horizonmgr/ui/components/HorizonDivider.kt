package org.wvt.horizonmgr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HorizonDivider(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    thickness: Dp = 1.dp,
    startIndent: Dp = 0.dp
) {
    val indentMod = if (startIndent.value != 0f) {
        Modifier.padding(start = startIndent)
    } else {
        Modifier
    }
    Box(
        modifier.then(indentMod)
            .width(thickness)
            .fillMaxHeight()
            .background(color = color.copy(DividerAlpha))
    )
}

private const val DividerAlpha = 0.12f
