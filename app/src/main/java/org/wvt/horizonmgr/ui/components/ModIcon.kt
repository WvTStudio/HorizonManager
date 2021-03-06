package org.wvt.horizonmgr.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.compositeOver

@Composable
fun ModIcon(modifier: Modifier, image: ImageBitmap?) {
    Surface(modifier, color = MaterialTheme.colors.onBackground.copy(0.12f)
        .compositeOver(MaterialTheme.colors.background),) {
        Crossfade(targetState = image) { image ->
            if (image != null) {
                ImageWithoutQualityFilter(modifier = modifier, imageBitmap = image)
            }
        }
    }
}