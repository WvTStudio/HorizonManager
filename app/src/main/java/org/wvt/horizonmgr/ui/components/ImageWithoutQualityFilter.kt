package org.wvt.horizonmgr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

@Composable
fun ImageWithoutQualityFilter(
    modifier: Modifier,
    imageBitmap: ImageBitmap
) {
    var size by remember { mutableStateOf(IntSize(0, 0)) }
    Canvas(modifier = modifier.onSizeChanged { size = it }) {
        drawIntoCanvas { canvas ->
            canvas.drawImageRect(
                image = imageBitmap,
                paint = Paint().apply {
                    filterQuality = if (imageBitmap.height <= size.height && imageBitmap.width <= size.width) {
                        FilterQuality.None
                    } else {
                        FilterQuality.High
                    }
                },
                dstSize = size
            )
        }
    }
}