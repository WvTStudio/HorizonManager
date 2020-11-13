package org.wvt.horizonmgr.ui.news

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.components.NetworkImage

@Composable
fun News(
    onNavClick: () -> Unit
) {
    val vm = dependenciesViewModel<NewsViewModel>()
    val news by vm.news.collectAsState()
    val state by vm.state.collectAsState()
    val context = ContextAmbient.current

    onCommit(vm) { vm.refresh() }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().zIndex(4.dp.value),
            title = { Text("推荐资讯") },
            navigationIcon = {
                IconButton(onClick = onNavClick) {
                    Icon(Icons.Filled.Menu)
                }
            },
            backgroundColor = MaterialTheme.colors.surface
        )
        Crossfade(current = state) {
            when (it) {
                is NewsViewModel.State.Loading -> Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                is NewsViewModel.State.Succeed -> ScrollableColumn(Modifier.fillMaxSize()) {
                    news.forEach {
                        when (it) {
                            is NewsViewModel.News.Article -> {
                                NewsItem(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    title = it.title,
                                    brief = it.brief,
                                    coverUrl = it.coverUrl,
                                    onClick = { vm.newsDetail(context, it.id) }
                                )
                            }
                        }
                    }
                }
                is NewsViewModel.State.Error -> Box(Modifier.fillMaxSize()) {
                    ErrorPage(message = {
                        Text("加载出错")
                    }, onRetryClick = {
                        vm.refresh()
                    })
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
    coverUrl: String,
    onClick: () -> Unit
) {
    val cutBrief = remember(brief) {
        if (brief.length > 64) brief.substring(0, 160) + "..."
        else brief
    }
    Card(modifier = modifier.clickable(onClick = onClick), elevation = 2.dp) {
        Box(Modifier.fillMaxWidth().wrapContentHeight()) {
            // Cover Image
            NetworkImage(
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
                url = coverUrl,
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.wrapContentHeight()
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