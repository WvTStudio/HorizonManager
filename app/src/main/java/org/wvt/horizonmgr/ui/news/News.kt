package org.wvt.horizonmgr.ui.news

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@Composable
fun News(
    viewModel: NewsViewModel,
    onNavClick: () -> Unit
) {
    val news by viewModel.news.collectAsState()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    DisposableEffect(viewModel) {
        viewModel.refresh()
        onDispose { }
    }

    NewsUI(
        onNavClick = onNavClick,
        state = state,
        news = news,
        onNewsClick = { if (it is NewsViewModel.News.Article) viewModel.newsDetail(context, it.id) },
        onRefreshClick = { viewModel.refresh() }
    )
}

@Composable
private fun NewsUI(
    onNavClick: () -> Unit,
    state: NewsViewModel.State,
    news: List<NewsViewModel.News>,
    onNewsClick: (NewsViewModel.News) -> Unit,
    onRefreshClick: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(4.dp.value),
            title = { Text("推荐资讯") },
            navigationIcon = {
                IconButton(onClick = onNavClick) {
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "菜单")
                }
            },
            backgroundColor = MaterialTheme.colors.surface
        )
        Crossfade(state) {
            when (it) {
                is NewsViewModel.State.Loading -> Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                is NewsViewModel.State.Succeed -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
                    ) {
                        items(news) { item ->
                            when {
                                item is NewsViewModel.News.Article && item.coverUrl != null -> NewsItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 16.dp),
                                    title = item.title,
                                    brief = item.brief,
                                    onClick = { onNewsClick(item) },
                                    coverImage = {
                                        NetworkImage(
                                            url = item.coverUrl,
                                            contentScale = ContentScale.Crop,
                                            contentDescription = "封面"
                                        )
                                    }
                                )
                                item is NewsViewModel.News.Article && item.coverUrl == null -> NewsItemNoCover(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 16.dp),
                                    title = item.title,
                                    brief = item.brief,
                                    onClick = { onNewsClick(item) }
                                )
                            }
                        }
                    }
                }
                is NewsViewModel.State.Error -> Box(Modifier.fillMaxSize()) {
                    ErrorPage(
                        modifier = Modifier.align(Alignment.Center),
                        message = {
                            Text("加载出错")
                        }, onRetryClick = onRefreshClick
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsItemNoCover(
    modifier: Modifier = Modifier,
    title: String,
    brief: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Card(
        modifier = modifier,
        elevation = animateDpAsState(
            if (isPressed) 8.dp else 1.dp
        ).value
    ) {
        Column(
            modifier = Modifier
                .clickable(
                    onClick = onClick,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current
                )
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            // Brief
            Text(
                text = brief,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NewsItem(
    modifier: Modifier = Modifier,
    title: String,
    brief: String,
    coverImage: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Card(modifier = modifier, elevation = animateDpAsState(if (isPressed) 8.dp else 1.dp).value) {
        Box(
            Modifier
                .clickable(
                    onClick = onClick,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current
                )
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Cover Image
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f),
                content = coverImage
            )
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
                // Brief
                Text(
                    text = brief,
                    style = MaterialTheme.typography.body2,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
private fun NewsPreview() {
    PreviewTheme {
        NewsUI(
            onNavClick = {},
            state = NewsViewModel.State.Succeed,
            news = remember {
                listOf(
                    NewsViewModel.News.Article(
                        0,
                        "Test article",
                        "Test brief",
                        "http://aaa.aaa",
                        "2020-2020"
                    ),
                    NewsViewModel.News.Article(
                        0,
                        "Test article",
                        "Test brief",
                        null,
                        "2020-2020"
                    )
                )
            },
            onNewsClick = {},
            onRefreshClick = {}
        )
    }
}