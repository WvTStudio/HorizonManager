package org.wvt.horizonmgr.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun ArticleItemStyle2(
    title: String,
    brief: String,
    sendTime: String,
    coverImage: Painter?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(modifier = modifier, onClick = onClick, shape = RoundedCornerShape(8.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Cover Image
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f),
                color = MaterialTheme.colors.onSurface.copy(0.12f)
            ) {
                AnimatedVisibility(
                    visible = coverImage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Image(
                        painter = coverImage ?: remember { ColorPainter(Color.Transparent) },
                        contentDescription = stringResource(R.string.home_screen_item_cover_desc),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.32f))
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                // Brief
                Text(
                    text = brief,
                    style = MaterialTheme.typography.body2,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                // Time
                Text(
                    text = sendTime,
                    style = MaterialTheme.typography.caption,
                    color = Color.White
                )
            }
        }
    }
}

@Preview
@Composable
private fun ArticleItemStyle2Preview() {
    PreviewTheme {
        Box(Modifier.padding(16.dp)) {
            ArticleItemStyle2(
                title = "This is a example title, this article does not have cover image.",
                brief = "This is one of the three styles of article card, this style displays when there is not cover image of article.",
                sendTime = "1 hour ago",
                coverImage = null,
                onClick = {}
            )
        }
    }
}
