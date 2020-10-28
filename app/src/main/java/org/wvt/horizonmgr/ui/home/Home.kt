package org.wvt.horizonmgr.ui.home

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun Home(
    onNavClick: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = { Text("首页资讯") },
            navigationIcon = {
                IconButton(onClick = onNavClick) {
                    Icon(Icons.Filled.Menu)
                }
            },
            backgroundColor = MaterialTheme.colors.surface
        )
        Box(Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "暂不支持"
            )
        }
    }
}

@Composable
fun ItemCard() {
    Card {
        
    }
}