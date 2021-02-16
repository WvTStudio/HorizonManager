package org.wvt.horizonmgr.ui.components

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(url) {
        image = try {
            loadUrlImage(context, url)
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO: 2020/11/13 Error Image
            null
        }
    }

    Surface(
        modifier = modifier,
        color = backgroundColor
    ) {
        Crossfade(image) {
            if (it != null) {
                Image(it, contentDescription, Modifier.fillMaxSize(), alignment, contentScale, alpha, colorFilter)
            }
        }
    }
}

private suspend fun loadUrlImage(context: Context, url: String): ImageBitmap =
    suspendCancellableCoroutine { cont ->
        try {
            val glide = Glide.with(context)

            val target = object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    try {
                        cont.resume(resource.asImageBitmap())
                    } catch (e: Exception) {
                        try {
                            cont.resumeWithException(e)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    try {
                        cont.resumeWithException(IllegalStateException("Load cleared"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    try {
                        cont.resumeWithException(IllegalStateException("Load failed"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            glide.asBitmap().load(url).into(target)
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }