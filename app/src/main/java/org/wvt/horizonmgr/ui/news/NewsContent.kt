package org.wvt.horizonmgr.ui.news

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.components.MarkdownContent
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor

@Composable
fun NewsContent(
    id: String,
    vm: NewsContentViewModel,
    onNavClick: () -> Unit
) {
    val news by vm.content.collectAsState()
    val refreshing by vm.isRefreshing.collectAsState()

    LaunchedEffect(id) { vm.load(id) }

    Column {
        TopAppBar(
            modifier = Modifier.zIndex(4.dp.value),
            navigationIcon = {
                IconButton(onClick = onNavClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                }
            }, title = {
                Text("资讯正文")
            }, backgroundColor = AppBarBackgroundColor
        )
        Crossfade(news) {
            when (it) {
                NewsContentViewModel.Result.Loading -> Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                NewsContentViewModel.Result.NetworkError -> {
                    ErrorPage(
                        modifier = Modifier.fillMaxSize(),
                        message = { Text("网络错误，请稍后再试") },
                        onRetryClick = { vm.refresh() }
                    )
                }
                NewsContentViewModel.Result.NewsNotFound -> {
                    ErrorPage(
                        modifier = Modifier.fillMaxSize(),
                        message = { Text("该资讯可能已被删除") },
                        onRetryClick = { vm.refresh() }
                    )
                }
                NewsContentViewModel.Result.OtherError -> {
                    ErrorPage(
                        modifier = Modifier.fillMaxSize(),
                        message = { Text("未知错误，请稍后再试") },
                        onRetryClick = { vm.refresh() }
                    )
                }
                is NewsContentViewModel.Result.Succeed -> {
                    Content(it.value, refreshing, vm::refresh)
                }
            }
        }
    }
}


@Composable
private fun Content(
    content: NewsContentViewModel.NewsContent,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    SwipeRefresh(state = rememberSwipeRefreshState(isRefreshing), onRefresh = onRefresh,
        indicator = { state, distance ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = distance,
                contentColor = MaterialTheme.colors.primary
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (content.coverImage != null) {
                NetworkImage(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(4.dp)),
                    url = content.coverImage,
                    contentDescription = "封面",
                    contentScale = ContentScale.Crop
                )
            }

            // Title
            SelectionContainer {
                Text(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp
                    ),
                    text = content.title, style = MaterialTheme.typography.h5
                )
            }

            // Brief
            SelectionContainer {
                Text(
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 16.dp
                    ),
                    text = content.brief,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(0.54f)
                )
            }

            Divider(
                Modifier
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )

            // Content
            /*SelectionContainer {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .fillParentMaxWidth(),
                    text = content.content,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(0.74f)
                )
            }*/
            MarkdownContent(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                md = content.content
            )
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
            content = {}
        )
    }
}

