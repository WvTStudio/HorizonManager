package org.wvt.horizonmgr.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.theme.PreviewTheme

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
                    Icon(Icons.Rounded.ArrowForward, stringResource(R.string.statefab_action_next))
                }
                FabState.LOADING -> CircularProgressIndicator()
                FabState.SUCCEED -> Icon(
                    imageVector = Icons.Rounded.Check,
                    tint = MaterialTheme.colors.secondary,
                    contentDescription = stringResource(R.string.statefab_state_succeed)
                )
                FabState.FAILED -> Icon(
                    imageVector = Icons.Rounded.Clear,
                    tint = MaterialTheme.colors.error,
                    contentDescription = stringResource(R.string.statefab_state_failed)
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewTodo() {
    PreviewTheme {
        StateFab(state = FabState.TODO, {})
    }
}

@Preview
@Composable
private fun PreviewLoading() {
    PreviewTheme {
        StateFab(state = FabState.LOADING, {})
    }
}

@Preview
@Composable
private fun PreviewSucceed() {
    PreviewTheme {
        StateFab(state = FabState.SUCCEED, {})
    }
}

@Preview
@Composable
private fun PreviewFailed() {
    PreviewTheme {
        StateFab(state = FabState.FAILED, {})
    }
}