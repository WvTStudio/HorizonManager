package org.wvt.horizonmgr.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.pressIndicatorGestureFilter
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R

private val shrinkTween = tween<Float>(250, 40, LinearOutSlowInEasing)
private val expandTween = tween<Float>(250, 0, LinearOutSlowInEasing)

@Composable
fun AnimateLogo(modifier: Modifier = Modifier) {
    val iconBack = imageResource(id = R.mipmap.icon_background)
    val iconFore = imageResource(id = R.mipmap.icon_foreground)

    // 扳手的摇摆动画
    val shakeRotation by rememberInfiniteTransition().animateFloat(initialValue = -45f, targetValue = 45f, animationSpec = InfiniteRepeatableSpec(
        tween(durationMillis = 900, easing = FastOutSlowInEasing),
        RepeatMode.Reverse
    ))

    // 齿轮的旋转动画
    val gearRotation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = InfiniteRepeatableSpec(
            tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing,
                delayMillis = 2000,
            ),
            RepeatMode.Restart
        )
    )
    var pressed by remember { mutableStateOf(false) }

    val scaleAnim =
        animateFloatAsState(
            if (pressed) 1.4f else 1f,
            if (pressed) expandTween else shrinkTween
        ).value

    Surface(
        modifier = modifier
            .size(64.dp)
            .pressIndicatorGestureFilter(
                onStart = { pressed = true },
                onCancel = { pressed = false },
                onStop = { pressed = false }
            ),
        elevation = 16.dp,
        shape = RoundedCornerShape(50)
    ) {
        Canvas(
            modifier = Modifier.size(64.dp)
        ) {
            scale(scaleAnim) {
                rotate(shakeRotation) {
                    drawImage(iconBack, dstSize = IntSize(64.dp.toIntPx(), 64.dp.toIntPx()))
                }
                rotate(gearRotation) {
                    drawImage(iconFore, dstSize = IntSize(64.dp.toIntPx(), 64.dp.toIntPx()))
                }
            }
        }
    }
}