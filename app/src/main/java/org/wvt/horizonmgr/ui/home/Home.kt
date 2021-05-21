package org.wvt.horizonmgr.ui.home

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@Composable
fun HomeScreen(
    onNavClick: () -> Unit,
    onNewsClick: (HomeViewModel.ContentResource) -> Unit
) {
    Home(viewModel = hiltViewModel(), onNavClick = onNavClick, onNewsClick = onNewsClick)
}

@Composable
fun Home(
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

    NewsUI(
        onNavClick = onNavClick,
        state = state,
        home = news,
        onNewsClick = { onNewsClick(it) },
        onRefresh = { viewModel.refresh() },
        isRefreshing = viewModel.isRefreshing.collectAsState().value
    )
}

@Composable
private fun NewsUI(
    onNavClick: () -> Unit,
    state: HomeViewModel.State,
    home: List<HomeViewModel.ContentResource>,
    onNewsClick: (HomeViewModel.ContentResource) -> Unit,
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
                    NewsList(
                        home,
                        onNewsClick,
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
private fun NewsList(
    home: List<HomeViewModel.ContentResource>,
    onNewsClick: (HomeViewModel.ContentResource) -> Unit,
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
        ) {
            items(home) { item ->
                when {
                    item is HomeViewModel.ContentResource.Article && item.coverUrl != null -> NewsItem(
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
                    item is HomeViewModel.ContentResource.Article && item.coverUrl == null -> NewsItemNoCover(
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
            state = HomeViewModel.State.Succeed,
            home = remember {
                listOf(
                    HomeViewModel.ContentResource.Article(
                        "",
                        "Test article",
                        "Test brief",
                        "http://aaa.aaa",
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
            onNewsClick = {},
            onRefresh = {},
            isRefreshing = false
        )
    }
}