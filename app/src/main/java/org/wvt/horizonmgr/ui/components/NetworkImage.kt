package org.wvt.horizonmgr.ui.components

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest

@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    val image by loadUrlImage(url = url)

    Box(modifier = modifier.background(backgroundColor)) {
        Crossfade(image) {
            if (it != null) {
                Image(
                    it,
                    contentDescription,
                    Modifier.fillMaxSize(),
                    alignment,
                    contentScale,
                    alpha,
                    colorFilter
                )
            }
        }
    }
}

@Composable
fun loadUrlImage(url: String): State<ImageBitmap?> {
    val image = remember(url) { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(url) {
        image.value = loadUrlImage(context, url)
    }
    
    return image
}

private suspend fun loadUrlImage(context: Context, url: String): ImageBitmap? {
    val imageLoader = ImageLoader.Builder(context).build()
    val request = ImageRequest.Builder(context)
        .data(url)
        .build()
    val disposable = imageLoader.execute(request)
    return disposable.drawable?.toBitmap()?.asImageBitmap()
}