package org.wvt.horizonmgr.ui.components

import androidx.compose.animation.animatedFloat
import androidx.compose.animation.core.AnimatedFloat
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Layout
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawLayer
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMaxBy

@Composable
fun <T> ImprovedCrossfade(
    current: T,
    modifier: Modifier = Modifier,
    animation: AnimationSpec<Float> = tween(),
    children: @Composable (T) -> Unit
) {
    val state = remember { CrossfadeState<T>() }
    if (current != state.current) {
        state.current = current
        val keys = state.items.map { it.key }.toMutableList()
        if (!keys.contains(current)) {
            keys.add(current)
        }
        state.items.clear()
        keys.mapTo(state.items) { key ->
            CrossfadeAnimationItem(key) { children ->
                val opacity = animatedOpacity(
                    animation = animation,
                    visible = key == current,
                    onAnimationFinish = {
                        if (key == state.current) {
                            // leave only the current in the list
                            state.items.removeAll { it.key != state.current }
                            state.invalidate()
                        }
                    }
                )
                Layout(
                    modifier = Modifier.drawLayer(alpha = opacity.value, clip = false),
                    children = children,
                    measureBlock = { m, c ->
                        val p = m.map { it.measure(c) }
                        val maxHeight = p.fastMaxBy { it.height }?.height ?: 0
                        val maxWidth = p.fastMaxBy { it.width }?.width ?: 0
                        layout(maxHeight, maxWidth) {
                            p.fastForEach {
                                it.place(0, 0)
                            }
                        }
                    })
            }
        }
    }
    Layout(
        modifier = modifier,
        children = {
            state.invalidate = invalidate
            state.items.fastForEach { (item, opacity) ->
                key(item) {
                    opacity {
                        children(item)
                    }
                }
            }
        }, measureBlock = { m, c ->
            val p = m.map { it.measure(c) }
            val maxHeight = p.fastMaxBy { it.height }?.height ?: 0
            val maxWidth = p.fastMaxBy { it.width }?.width ?: 0
            layout(maxHeight, maxWidth) {
                p.fastForEach {
                    it.place(0, 0)
                }
            }
        })
}

private class CrossfadeState<T> {
    // we use Any here as something which will not be equals to the real initial value
    var current: Any? = Any()
    var items = mutableListOf<CrossfadeAnimationItem<T>>()
    var invalidate: () -> Unit = { }
}

private data class CrossfadeAnimationItem<T>(
    val key: T,
    val transition: CrossfadeTransition
)

private typealias CrossfadeTransition = @Composable (children: @Composable () -> Unit) -> Unit

@Composable
private fun animatedOpacity(
    animation: AnimationSpec<Float>,
    visible: Boolean,
    onAnimationFinish: () -> Unit = {}
): AnimatedFloat {
    val animatedFloat = animatedFloat(if (!visible) 1f else 0f)
    onCommit(visible) {
        animatedFloat.animateTo(
            if (visible) 1f else 0f,
            anim = animation,
            onEnd = { reason, _ ->
                if (reason == AnimationEndReason.TargetReached) {
                    onAnimationFinish()
                }
            })
    }
    return animatedFloat
}
