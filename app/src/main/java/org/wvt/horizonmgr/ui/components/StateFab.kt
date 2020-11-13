package org.wvt.horizonmgr.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
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
    Box(modifier.size(56.dp), Alignment.Center) {
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
    }
}