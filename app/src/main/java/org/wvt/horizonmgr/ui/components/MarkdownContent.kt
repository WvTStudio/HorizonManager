package org.wvt.horizonmgr.ui.components

import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin

@Composable
fun MarkdownContent(modifier: Modifier, md: String) {
    val context = LocalContext.current
    val textColor by rememberUpdatedState(newValue = MaterialTheme.colors.onBackground.copy(ContentAlpha.high))
    val view = remember {
        TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            textSize = 16f
            setTextIsSelectable(true)
        }
    }

    val markwon = remember {
        val imageLoader = ImageLoader.Builder(context)
            .apply {
                availableMemoryPercentage(0.5)
                bitmapPoolPercentage(0.5)
                crossfade(true)
            }
            .build()

        val coilPlugin = CoilImagesPlugin.create(
            object : CoilImagesPlugin.CoilStore {
                override fun load(drawable: AsyncDrawable): ImageRequest {
                    return ImageRequest.Builder(context)
                        .defaults(imageLoader.defaults)
                        .data(drawable.destination)
                        .crossfade(true)
                        .build()
                }

                override fun cancel(disposable: Disposable) {
                    disposable.dispose()
                }
            },
            imageLoader
        )
        Markwon.builder(context).apply {
            usePlugin(MarkwonInlineParserPlugin.create())
            usePlugin(coilPlugin)
            usePlugin(TablePlugin.create(context))
            usePlugin(JLatexMathPlugin.create(view.textSize) { builder ->
                builder.inlinesEnabled(true)
            })
        }.build()
    }

    DisposableEffect(md) {
        markwon.setMarkdown(view, md)
        onDispose { }
    }

    AndroidView(modifier = modifier, factory = { view }, update = {
        it.setTextColor(textColor.toArgb())
    })
}