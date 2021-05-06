package org.wvt.horizonmgr.ui.fileselector

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.ui.theme.PreviewTheme
import kotlin.math.roundToInt

private const val TAG = "FolderItem"

private enum class SwipeState {
    YES, NO
}

/**
 * 带左滑收藏功能的文件夹列表项
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun FolderItem(
    name: String,
    onClick: () -> Unit,
    isStared: Boolean,
    onStarChange: (isStared: Boolean) -> Unit,
) {
    val swipePoint = with(LocalDensity.current) { 74.dp.toPx() }
    val anchors = mapOf(0f to SwipeState.NO, -swipePoint to SwipeState.YES)

    val state = rememberSwipeableState(SwipeState.NO) {
        Log.d(TAG, "confirm change: $it")
        if (it == SwipeState.YES) {
            onStarChange(!isStared)
        }
        false
    }

    val starState = remember(state.offset.value, swipePoint, isStared) {
        val offset = if(state.offset.value.isNaN()) 0f else state.offset.value
        (offset > -swipePoint - 10) xor !isStared
    }

    Box(
        Modifier
            .wrapContentSize()
            .clickable(onClick = onClick)
            .swipeable(
                state = state,
                anchors = anchors,
                orientation = Orientation.Horizontal,
                thresholds = { _, _ -> FractionalThreshold(1f) }
            )
    ) {
        ToggleBackgroundWithIcon(
            modifier = Modifier.matchParentSize(),
            check = starState,
            gravity = Alignment.End,
            activeColor = MaterialTheme.colors.secondary,
            inactiveColor = MaterialTheme.colors.onBackground.copy(0.12f)
                .compositeOver(MaterialTheme.colors.background),
            iconActiveColor = MaterialTheme.colors.onSecondary,
            iconInactiveColor = MaterialTheme.colors.onBackground
        )
        ListItem(
            modifier = Modifier
                .clickable(onClick = onClick)
                .offset(offset = { IntOffset(x = state.offset.value.roundToInt(), y = 0) })
                .background(MaterialTheme.colors.surface),
            icon = { Icon(Icons.Filled.Folder, contentDescription = "文件夹") },
            text = { Text(name) }
        )
    }
}

private enum class IconState {
    OFF, ON
}

data class Ripple(
    val check: Boolean,
    val animatable: Animatable<Float, AnimationVector1D>
)

/**
 * check 状态：
 * primary 背景
 * 图标为 onPrimary
 *
 * uncheck 状态：
 * surface 背景
 * 图标为 onSurface
 *
 * 从 check 到 uncheck：
 * 图标抖动一下
 * surface 涟漪延展
 * 涟漪动画结束时，背景改为 surface，移除涟漪
 *
 * 从 uncheck 到 check：
 * 图标抖动
 * primary 涟漪延展
 * 动画结束时，背景改为 primary，移除涟漪
 */
@Composable
private fun ToggleBackgroundWithIcon(
    modifier: Modifier = Modifier,
    check: Boolean,
    gravity: Alignment.Horizontal,
    activeColor: Color,
    inactiveColor: Color,
    iconActiveColor: Color = contentColorFor(activeColor),
    iconInactiveColor: Color = contentColorFor(inactiveColor)
) {
    val scope = rememberCoroutineScope()
    var initialized by remember { mutableStateOf(false) }
    val ripples = remember { mutableSetOf<Ripple>() }
    var backgroundColor by remember { mutableStateOf(if (check) activeColor else inactiveColor) }

    DisposableEffect(check) {
        Log.d(TAG, "Check: $check")
        if (initialized) {
            scope.launch {
                val animatable = Animatable(0f)
                val ripple = Ripple(check, animatable)
                ripples.add(ripple)
                try {
                    animatable.animateTo(1f, tween())
                    backgroundColor = if (check) activeColor else inactiveColor
                } finally {
                    ripples.remove(ripple)
                }
            }
        } else {
            initialized = true
        }
        onDispose { }
    }

    Box(
        modifier.background(
            backgroundColor
        )
    ) {
        // Ripple
        BoxWithConstraints {
            for (ripple in ripples) {
                val fraction = ripple.animatable.asState().value
                val rippleSizePx = lerp(
                    0,
                    (constraints.maxHeight + constraints.maxWidth) * 2,
                    fraction
                )
                val padding = 40.dp
                Layout(
                    modifier = Modifier.graphicsLayer(clip = true),
                    content = {
                        // 涟漪
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (ripple.check) activeColor else inactiveColor)
                                .size(with(LocalDensity.current) { rippleSizePx.toDp() })
                        )
                    }
                ) { list: List<Measurable>, constraints: Constraints ->
                    val p = list[0].measure(constraints)
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        p.place(
                            x = if (gravity == Alignment.Start) {
                                padding.roundToPx() - p.width / 2
                            } else {
                                constraints.maxWidth - padding.roundToPx() - p.width / 2
                            },
                            y = (constraints.maxHeight - p.height) / 2
                        )
                    }
                }
            }
        }

        // Icon
        SwitchIcon(
            modifier = Modifier
                .align(
                    if (gravity == Alignment.Start) Alignment.CenterStart
                    else Alignment.CenterEnd
                )
                .padding(horizontal = 28.dp),
            isActive = check,
            activeIcon = Icons.Filled.Star,
            activeColor = iconActiveColor,
            inactiveIcon = Icons.Filled.StarBorder,
            inactiveColor = iconInactiveColor,
        )
    }
}

/**
 * 可切换的图标，在切换时会跳动一下并更换颜色
 */
@Composable
private fun SwitchIcon(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    activeIcon: ImageVector,
    activeColor: Color,
    inactiveIcon: ImageVector,
    inactiveColor: Color
) {
    val iconColor = animateColorAsState(
        if (isActive) activeColor
        else inactiveColor
    ).value

    val transition = updateTransition(targetState = if (isActive) IconState.ON else IconState.OFF,
        label = "transition"
    )
    val scale by transition.animateFloat(
        transitionSpec = {
            keyframes {
                durationMillis = 160
                1f at 0
                1.2f at 40 with FastOutLinearInEasing
                1f at 160 with LinearOutSlowInEasing
            }
        },
        targetValueByState = {
            if(it == IconState.ON) 1f else 1f
        }, label = "scale"
    )

    Icon(
        modifier = modifier.graphicsLayer(
            scaleX = scale,
            scaleY = scale
        ),
        imageVector = if (isActive) activeIcon else inactiveIcon,
        tint = iconColor,
        contentDescription = null
    )
}

@Preview
@Composable
private fun FolderEntryPreview() {
    PreviewTheme(Modifier) {
        FolderItem(
            name = "Test File",
            onClick = {},
            isStared = false,
            onStarChange = {}
        )
    }
}