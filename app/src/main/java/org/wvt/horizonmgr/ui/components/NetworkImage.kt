package org.wvt.horizonmgr.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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
import androidx.compose.ui.platform.ContextAmbient
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

@Composable
fun NetworkImage(
    url: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    var image by remember { mutableStateOf<ImageAsset?>(null) }
    val context = ContextAmbient.current
    onActive {
        val glide = Glide.with(context)
        val target = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                image = resource.asImageAsset()
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                image = null
            }
        }
        glide.asBitmap().load(url).into(target)
        onDispose {
            glide.clear(target)
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