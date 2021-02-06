package org.wvt.horizonmgr.ui.news

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@Composable
fun News(
    onNavClick: () -> Unit
) {
    val vm = dependenciesViewModel<NewsViewModel>()
    val news by vm.news.collectAsState()
    val state by vm.state.collectAsState()
    val context = AmbientContext.current

    DisposableEffect(vm) {
        vm.refresh()
        onDispose { }
    }

    NewsUI(
        onNavClick = onNavClick,
        state = state,
        news = news,
        onNewsClick = { if (it is NewsViewModel.News.Article) vm.newsDetail(context, it.id) },
        onRefreshClick = { vm.refresh() }
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
        Crossfade(current = state) {
            when (it) {
                is NewsViewModel.State.Loading -> Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                is NewsViewModel.State.Succeed -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            news.forEach { item ->
                                when (item) {
                                    is NewsViewModel.News.Article -> {
                                        NewsItem(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
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
                                    }
                                }
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
private fun NewsItem(
    modifier: Modifier = Modifier,
    title: String,
    brief: String,
    coverImage: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val cutBrief = remember(brief) {
        if (brief.length > 64) brief.substring(0, 160) + "..."
        else brief
    }
    Card(modifier = modifier.clickable(onClick = onClick), elevation = 2.dp) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Cover Image
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
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
                    color = Color.White
                )
                // Brief
                Text(
                    text = cutBrief,
                    style = MaterialTheme.typography.body2,
                    color = Color.White
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
                        "http://aaa.aaa"
                    )
                )
            },
            onNewsClick = {},
            onRefreshClick = {}
        )
    }
}