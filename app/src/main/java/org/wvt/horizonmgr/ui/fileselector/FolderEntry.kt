package org.wvt.horizonmgr.ui.fileselector

import android.annotation.SuppressLint
import androidx.compose.animation.animate
import androidx.compose.animation.core.*
import androidx.compose.animation.transition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.theme.PreviewTheme

private enum class IconState {
    OFF, ON
}

private val iconScaleKey = FloatPropKey()

@SuppressLint("Range")
private val iconTransition = transitionDefinition<IconState> {
    state(IconState.OFF) {
        set(iconScaleKey, 1f)
    }
    state(IconState.ON) {
        set(iconScaleKey, 1f)
    }
    transition(
        IconState.ON to IconState.OFF,
        IconState.OFF to IconState.ON
    ) {
        iconScaleKey using keyframes {
            durationMillis = 160
            1f at 0
            1.2f at 80 with FastOutLinearInEasing
            1f at 160 with LinearOutSlowInEasing
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun FolderEntry(
    name: String,
    onClick: () -> Unit,
    isStared: Boolean,
    onStarChange: (isStared: Boolean) -> Unit,
) {
    val swipePoint = with(AmbientDensity.current) { 72.dp.toPx() }
    val anchors = mapOf(0f to 0, -swipePoint to 1)

    val state = rememberSwipeableState(0) {
        onStarChange(isStared)
        false
    }

    val starState = (state.offset.value > -swipePoint - 10) xor !isStared

    val backgroundColor = animate(
        if (starState) MaterialTheme.colors.secondary
        else Color(0xFFC7C7C7)
    )

    val iconColor = animate(
        if (starState) MaterialTheme.colors.onSecondary
        else MaterialTheme.colors.onSurface
    )

    val iconTransitionState = transition(
        definition = iconTransition,
        initState = IconState.OFF,
        toState = if (starState) IconState.ON else IconState.OFF
    )

    val iconScale = iconTransitionState[iconScaleKey]

    Box(
        Modifier.wrapContentSize()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .swipeable(
                state = state,
                anchors = anchors,
                orientation = Orientation.Horizontal,
                thresholds = { _, _ -> FractionalThreshold(0.2f) }
            )
    ) {
        Icon(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp).graphicsLayer(
                scaleX = iconScale,
                scaleY = iconScale
            ),
            imageVector = if (starState) Icons.Filled.Star else Icons.Filled.StarBorder,
            tint = iconColor
        )

        ListItem(
            modifier = Modifier.clickable(onClick = onClick)
                .offset(x = { state.offset.value })
                .background(MaterialTheme.colors.surface),
            icon = { Icon(Icons.Filled.Folder) },
            text = { Text(name) }
        )
    }
}


@Preview
@Composable
private fun FolderEntryPreview() {
    PreviewTheme(Modifier) {
        FolderEntry(
            name = "Test File",
            onClick = {},
            isStared = false,
            onStarChange = {}
        )
    }
}