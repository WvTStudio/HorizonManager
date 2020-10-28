package org.wvt.horizonmgr.ui.components

import androidx.compose.animation.animate
import androidx.compose.animation.core.*
import androidx.compose.animation.transition
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

private val rotation = FloatPropKey()
private val shake = transitionDefinition<Int> {
    state(1) {
        this[rotation] = -45f
    }
    state(2) {
        this[rotation] = 45f
    }
    transition(1 to 2, 2 to 1) {
        rotation using repeatable(
            animation = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            ),
            iterations = AnimationConstants.Infinite,
            repeatMode = RepeatMode.Reverse
        )
    }
}

private val rotationFore = FloatPropKey()
private val rotateFore = transitionDefinition<Int> {
    state(1) {
        this[rotationFore] = 0f
    }
    state(2) {
        this[rotationFore] = -360f
    }
    transition(1 to 2, 2 to 1) {
        rotationFore using repeatable(
            animation = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing,
                delayMillis = 2000,
            ),
            iterations = AnimationConstants.Infinite
        )
    }
}

private val shrinkTween = tween<Float>(250, 40, LinearOutSlowInEasing)
private val expandTween = tween<Float>(250, 0, LinearOutSlowInEasing)

@Composable
fun AnimateLogo(modifier: Modifier = Modifier) {
    val iconBack = imageResource(id = R.mipmap.icon_background)
    val iconFore = imageResource(id = R.mipmap.icon_foreground)
    val anim = transition(definition = shake, initState = 1, toState = 2)
    val animFore = transition(definition = rotateFore, initState = 1, toState = 2)

    var pressed by remember { mutableStateOf(false) }

    val scaleAnim = animate(if (pressed) 1.4f else 1f, if (pressed) expandTween else shrinkTween)

    Surface(
        modifier = modifier.size(64.dp)
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
                rotate(anim[rotation]) {
                    drawImage(iconBack, dstSize = IntSize(64.dp.toIntPx(), 64.dp.toIntPx()))
                }
                rotate(animFore[rotationFore]) {
                    drawImage(iconFore, dstSize = IntSize(64.dp.toIntPx(), 64.dp.toIntPx()))
                }
            }
        }
    }
}