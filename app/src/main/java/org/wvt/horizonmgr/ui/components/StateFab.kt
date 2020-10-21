package org.wvt.horizonmgr.ui.components

import androidx.compose.foundation.Icon
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Layout
import androidx.compose.ui.Measurable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class FabState {
    TODO, LOADING, SUCCEED, FAILED
}

@Composable
fun StateFab(
    state: FabState,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val size = remember { 56.dp }
    ImproveCrossfade(
        current = state
    ) { state ->
        Layout(
            modifier = modifier,
            children = {
                when (state) {
                    FabState.TODO -> FloatingActionButton(
                        onClick = onClicked,
                        contentColor = MaterialTheme.colors.onSecondary
                    ) { Icon(Icons.Filled.ArrowForward) }
                    FabState.LOADING -> CircularProgressIndicator()
                    FabState.SUCCEED -> Icon(
                        asset = Icons.Filled.Check,
                        tint = MaterialTheme.colors.secondary
                    )
                    FabState.FAILED -> Icon(
                        asset = Icons.Filled.Clear,
                        tint = MaterialTheme.colors.error
                    )
                }
            }, measureBlock = { m: List<Measurable>, c ->
                val placeables = m.map { it.measure(c) }
                val sizePx = size.toIntPx()
                layout(sizePx, sizePx) {
                    placeables.forEach {
                        // place it to the center
                        it.place(
                            (sizePx - it.height) / 2,
                            (sizePx - it.width) / 2,
                        )
                    }
                }
            }
        )
    }
}