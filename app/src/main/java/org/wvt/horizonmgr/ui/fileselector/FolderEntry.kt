package org.wvt.horizonmgr.ui.fileselector

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.transition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.theme.PreviewTheme

/**
 * 带左滑收藏功能的文件夹列表项
 */
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

    Box(
        Modifier
            .wrapContentSize()
            .clickable(onClick = onClick)
            .swipeable(
                state = state,
                anchors = anchors,
                orientation = Orientation.Horizontal,
                thresholds = { _, _ -> FractionalThreshold(0.2f) }
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
                .offset(offset = { IntOffset(x = state.offset.value.toInt(), y = 0) })
                .background(MaterialTheme.colors.surface),
            icon = { Icon(Icons.Filled.Folder, contentDescription = "文件夹") },
            text = { Text(name) }
        )
    }
}

private enum class IconState {
    OFF, ON
}

private val iconScaleKey = FloatPropKey("Icon Scale")

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

private fun getTransition(
    start: Int, end: Int
): TransitionDefinition<String> = transitionDefinition {
    state("start") {
        set(sizeKey, start.toFloat())
    }
    state("end") {
        set(sizeKey, end.toFloat())
    }
    transition {
        sizeKey using tween()
    }
}

private val sizeKey = FloatPropKey(label = "size")

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
    val density = AmbientDensity.current
    var checkState by remember { mutableStateOf(check) }

    // 背景色会在涟漪结束后变换
    val backgroundColor = if (checkState) activeColor
    else inactiveColor

    val rippleColor = if (check) activeColor
    else inactiveColor

    var rippleStart by remember { mutableStateOf(false) }

    DisposableEffect(check) {
        rippleStart = true
        onDispose { }
    }

    Box(modifier.background(backgroundColor)) {
        BoxWithConstraints {
            val tr = transition(
                definition = getTransition(0, (constraints.maxHeight + constraints.maxWidth) * 2),
                initState = if (rippleStart) "start" else "end",
                toState = "end",
                onStateChangeFinished = {
                    checkState = check
                    rippleStart = false
                }
            )
            val padding = 36.dp
            Layout(
                modifier = Modifier.graphicsLayer(clip = true),
                content = {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(rippleColor)
                            .size(with(density) { tr[sizeKey].toDp() })
                    )
                }
            ) { list: List<Measurable>, constraints: Constraints ->
                val p = list[0].measure(constraints)
                layout(constraints.maxWidth, constraints.maxHeight) {
                    p.place(
                        x = if (gravity == Alignment.Start) {
                            padding.toIntPx() - p.width / 2
                        } else {
                            constraints.maxWidth - padding.toIntPx() - p.width / 2
                        },
                        y = (constraints.maxHeight - p.height) / 2
                    )
                }
            }
        }

        SwitchIcon(
            modifier = Modifier
                .align(
                    if (gravity == Alignment.Start) Alignment.CenterStart
                    else Alignment.CenterEnd
                )
                .padding(horizontal = 24.dp),
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

    val transition = updateTransition(targetState = if (isActive) IconState.ON else IconState.OFF)
    val scale by transition.animateFloat(
        transitionSpec = {
            keyframes {
                durationMillis = 160
                1f at 0
                1.2f at 80 with FastOutLinearInEasing
                1f at 160 with LinearOutSlowInEasing
            }
        },
        targetValueByState = {
            1f
        }
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

/*private class ItemState {
    @Volatile
    private var cont: Continuation<Unit>? = null

    suspend fun check() {
        suspendCoroutine<Unit> {
            cont = it
        }
    }
}*/

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