package org.wvt.horizonmgr.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ArticleItemNoCover(
    title: String,
    brief: String,
    sendTime: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(modifier = modifier, onClick = onClick, shape = RoundedCornerShape(8.dp)) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(16.dp)
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
private fun ArticleItemNoCoverPreview() {
    PreviewTheme {
        Box(Modifier.padding(16.dp)) {
            ArticleItemNoCover(
                title = "This is a example title, this article does not have cover image.",
                brief = "This is one of the three styles of article card, this style displays when there is not cover image of article.",
                sendTime = "1 hour ago",
                onClick = {}
            )
        }
    }
}