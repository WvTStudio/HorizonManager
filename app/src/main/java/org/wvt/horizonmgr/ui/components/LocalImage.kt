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

/**
 * 读取 [path] 图片，转换成 ImageBitmap 并显示
 * 在读取期间或读取失败时会使用灰色背景
 */
@Composable
fun LocalImage(
    path: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(path) {
        image = try {
            BitmapFactory.decodeFile(path).asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Surface(
        modifier = modifier,
        color = backgroundColor
    ) {
        Crossfade(current = image) {
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