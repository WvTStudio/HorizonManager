package org.wvt.horizonmgr.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.components.loadUrlImage
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor
import org.wvt.horizonmgr.ui.theme.PreviewTheme
import org.wvt.horizonmgr.viewmodel.HomeViewModel
import kotlin.random.Random

@Composable
fun HomeScreen(
    onNavClick: () -> Unit,
    onNewsClick: (HomeViewModel.ContentResource) -> Unit
) {
    HomeScreen(viewModel = hiltViewModel(), onNavClick = onNavClick, onNewsClick = onNewsClick)
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavClick: () -> Unit,
    onNewsClick: (HomeViewModel.ContentResource) -> Unit
) {
    val news by viewModel.contentResources.collectAsState()
    val state by viewModel.state.collectAsState()

    DisposableEffect(viewModel) {
        viewModel.load()
        onDispose { }
    }

    RecommendArticlesScreen(
        onNavClick = onNavClick,
        state = state,
        articles = news,
        onArticleClick = { onNewsClick(it) },
        onRefresh = { viewModel.refresh() },
        isRefreshing = viewModel.isRefreshing.collectAsState().value
    )
}

@Composable
private fun RecommendArticlesScreen(
    onNavClick: () -> Unit,
    state: HomeViewModel.State,
    articles: List<HomeViewModel.ContentResource>,
    onArticleClick: (HomeViewModel.ContentResource) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(4.dp.value),
            title = { Text("首页") },
            navigationIcon = {
                IconButton(onClick = onNavClick) {
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "菜单")
                }
            },
            backgroundColor = AppBarBackgroundColor
        )
        Crossfade(state) {
            when (it) {
                is HomeViewModel.State.Loading -> Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                is HomeViewModel.State.Succeed -> {
                    ArticleList(
                        articles,
                        onArticleClick,
                        isRefreshing,
                        onRefresh
                    )
                }
                is HomeViewModel.State.Error -> Box(Modifier.fillMaxSize()) {
                    ErrorPage(
                        modifier = Modifier.align(Alignment.Center),
                        message = {
                            Text("加载出错")
                        }, onRetryClick = onRefresh
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleList(
    home: List<HomeViewModel.ContentResource>,
    onArticleClick: (HomeViewModel.ContentResource) -> Unit,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = onRefreshClick,
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = trigger,
                contentColor = MaterialTheme.colors.primary
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(home) { item ->
                when {
                    item is HomeViewModel.ContentResource.Article && item.coverUrl != null -> {
                        ArticleItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = item.title,
                            brief = item.brief,
                            sendTime = item.updateTime,
                            coverImage = loadUrlImage(url = item.coverUrl).value?.let {
                                remember { BitmapPainter(it) }
                            }
                        ) {
                            onArticleClick(item)
                        }
                    }
                    item is HomeViewModel.ContentResource.Article && item.coverUrl == null -> ArticleItemNoCover(
                        modifier = Modifier.fillMaxWidth(),
                        title = item.title,
                        brief = item.brief,
                        onClick = { onArticleClick(item) },
                        sendTime = item.updateTime
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleItem(
    title: String,
    brief: String,
    sendTime: String,
    coverImage: Painter?,
    modifier: Modifier = Modifier,
    style: Int = remember { Random.nextInt(0, 3) },
    onClick: () -> Unit
) {
    when (style) {
        0 -> ArticleItemStyle1(title, brief, sendTime, coverImage, modifier, onClick)
        1 -> ArticleItemStyle2(title, brief, sendTime, coverImage, modifier, onClick)
        2 -> ArticleItemStyle3(title, brief, sendTime, coverImage, modifier, onClick)
    }
}

@Preview
@Composable
private fun NewsPreview() {
    PreviewTheme {
        Surface(color = MaterialTheme.colors.background) {
            RecommendArticlesScreen(
                onNavClick = {},
                state = HomeViewModel.State.Succeed,
                articles = remember {
                    listOf(
                        HomeViewModel.ContentResource.Article(
                            "",
                            "Test article",
                            "Test brief",
                            "",
                            "2020-2020"
                        ),
                        HomeViewModel.ContentResource.Article(
                            "",
                            "Test article",
                            "Test brief",
                            null,
                            "2020-2020"
                        )
                    )
                },
                onArticleClick = {},
                onRefresh = {},
                isRefreshing = false
            )
        }
    }
}