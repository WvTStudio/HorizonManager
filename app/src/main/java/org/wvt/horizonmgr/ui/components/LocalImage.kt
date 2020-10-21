package org.wvt.horizonmgr.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LocalImage(
    path: String?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    var image by remember { mutableStateOf<ImageAsset?>(null) }
    val scope = rememberCoroutineScope()

    onCommit(path) {
        if (path == null) return@onCommit
        scope.launch(Dispatchers.IO) {
            image = BitmapFactory.decodeFile(path).asImageAsset()
        }
    }

    Surface(
        modifier = modifier,
        color = backgroundColor
    ) {
        Crossfade(current = image) {
            if (it != null) {
                Image(it, Modifier.fillMaxSize(), alignment, contentScale, alpha, colorFilter)
            }
        }
    }
}