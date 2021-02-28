package org.wvt.horizonmgr.ui.onlinemods

import androidx.compose.animation.Crossfade
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import org.wvt.horizonmgr.ui.components.ImageWithoutQualityFilter
import org.wvt.horizonmgr.ui.components.loadUrlImage

@Composable
fun ModIcon(modifier: Modifier, url: String) {
    val image by loadUrlImage(url = url)
    Crossfade(targetState = image) { image ->
        if (image == null) {
            Surface(
                modifier,
                color = MaterialTheme.colors.onBackground.copy(0.12f)
                    .compositeOver(MaterialTheme.colors.background),
                content = {}
            )
        } else {
            ImageWithoutQualityFilter(modifier = modifier, imageBitmap = image)
        }
    }
}