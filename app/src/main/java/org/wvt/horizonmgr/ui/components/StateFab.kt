package org.wvt.horizonmgr.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    Crossfade(targetState = state) {
        // FIXME: 2021/3/11 在动画执行时，shadow 会被裁切
        Box(modifier.size(56.dp), Alignment.Center) {
            when (it) {
                FabState.TODO -> FloatingActionButton(
                    onClick = onClicked,
                    contentColor = MaterialTheme.colors.onSecondary
                ) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "下一步")
                }
                FabState.LOADING -> CircularProgressIndicator()
                FabState.SUCCEED -> Icon(
                    imageVector = Icons.Filled.Check,
                    tint = MaterialTheme.colors.secondary,
                    contentDescription = "成功"
                )
                FabState.FAILED -> Icon(
                    imageVector = Icons.Filled.Clear,
                    tint = MaterialTheme.colors.error,
                    contentDescription = "失败"
                )
            }
        }
    }
}