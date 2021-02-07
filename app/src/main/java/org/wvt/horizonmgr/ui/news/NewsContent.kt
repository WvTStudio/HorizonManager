package org.wvt.horizonmgr.ui.news

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.emptyContent
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.wvt.horizonmgr.ui.components.NetworkImage

@Composable
fun NewsContent(
    vm: NewsContentViewModel,
    onNavClick: () -> Unit
) {
    val news by vm.newsContent.collectAsState()

    Column {
        TopAppBar(
            modifier = Modifier.zIndex(4.dp.value),
            navigationIcon = {
                IconButton(onClick = onNavClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                }
            }, title = {
                Text("资讯正文")
            }, backgroundColor = MaterialTheme.colors.surface
        )
        Crossfade(news) { news ->
            if (news == null) {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Column(Modifier.fillParentMaxWidth()) {
                            NetworkImage(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillParentMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(4.dp)),
                                url = news.coverUrl,
                                contentDescription = "封面",
                                contentScale = ContentScale.Crop
                            )

                            // Title
                            Text(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                                text = news.title, style = MaterialTheme.typography.h5
                            )

                            // Brief
                            Text(
                                modifier = Modifier.padding(
                                    start = 24.dp,
                                    end = 24.dp,
                                    top = 16.dp
                                ),
                                text = news.brief,
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(0.54f)
                            )
                            
                            Divider(
                                Modifier
                                    .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                                    .fillParentMaxWidth()
                                    .align(Alignment.CenterHorizontally)
                            )

                            // Content
                            Text(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 24.dp)
                                    .fillParentMaxWidth(),
                                text = news.content,
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.onSurface.copy(0.74f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Label(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(modifier.wrapContentSize(unbounded = true)) {
        Text(modifier = Modifier.wrapContentWidth(), text = text)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colors.secondary,
            content = emptyContent()
        )
    }
}