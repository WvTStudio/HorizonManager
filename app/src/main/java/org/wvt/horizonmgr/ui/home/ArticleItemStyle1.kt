package org.wvt.horizonmgr.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun ArticleItemStyle1(
    title: String,
    brief: String,
    sendTime: String,
    coverImage: Painter?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(modifier = modifier, onClick = onClick, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                ) {
                    // Title
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    // Brief
                    Text(
                        text = brief,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(16.dp))
                Surface(
                    Modifier.size(96.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colors.onSurface.copy(0.12f)
                ) {
                    AnimatedVisibility(
                        visible = coverImage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Image(
                            painter = coverImage ?: remember { ColorPainter(Color.Transparent) },
                            contentDescription = "Cover Image",
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = sendTime,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)
            )
        }
    }
}


@Preview
@Composable
private fun ArticleItemStyle1Preview() {
    PreviewTheme {
        Box(Modifier.padding(16.dp)) {
            ArticleItemStyle1(
                title = "This is a example title, this article does not have cover image.",
                brief = "This is one of the three styles of article card, this style displays when there is not cover image of article.",
                sendTime = "1 hour ago",
                coverImage = null,
                onClick = {}
            )
        }
    }
}
