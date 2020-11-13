package org.wvt.horizonmgr.ui.joingroup

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.ui.components.NetworkImage

@Composable
fun JoinGroup(
    vm: JoinGroupViewModel,
    onClose: () -> Unit,
    onGroupSelect: (url: WebAPI.QQGroupEntry) -> Unit
) {
    val vmGroupList by vm.groups.collectAsState()

    // TODO: 2020/10/27 添加网络错误时的提示
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = {
            Text("加入群组")
        }, navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.ArrowBack)
            }
        }, backgroundColor = MaterialTheme.colors.surface)
        Crossfade(
            modifier = Modifier.fillMaxSize(),
            current = vmGroupList
        ) { groupList ->
            if (groupList.isEmpty()) {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            } else {
                ScrollableColumn(Modifier.fillMaxSize()) {
                    groupList.fastForEach {
                        GroupItem(
                            onClicked = { onGroupSelect(it) },
                            avatarUrl = it.avatar,
                            groupName = it.name,
                            description = it.description,
                            status = it.status
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupItem(
    onClicked: () -> Unit,
    avatarUrl: String, groupName: String, description: String, status: String
) {
    val emphasis = AmbientEmphasisLevels.current
    Row(Modifier.clickable(onClick = onClicked).padding(vertical = 16.dp)) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group Avatar
            NetworkImage(
                modifier = Modifier.size(42.dp)
                    .clip(RoundedCornerShape(percent = 50)),
                url = avatarUrl
            )
            // Tag
            StatusTag(
                modifier = Modifier.padding(top = 8.dp),
                text = status
            )
        }
        // Information
        Column(Modifier.weight(1f)) {
            // GroupName
            Providers(AmbientContentAlpha provides ContentAlpha.high) {
                Box(Modifier.fillMaxWidth()) {
                    // GroupName
                    Text(
                        modifier = Modifier.align(Alignment.CenterStart),
                        text = groupName,
                        style = MaterialTheme.typography.body1
                    )
                }
            }
            // Description
            Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = description, style = MaterialTheme.typography.body2
                )
            }
        }
        // Arrow Icon
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.ArrowForward)
        }
    }
}

@Composable
private fun StatusTag(
    text: String,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colors.primary.copy(0.4f)
) {
    Box(modifier.border(1.dp, contentColor, RoundedCornerShape(4.dp))) {
        Text(
            modifier = Modifier.align(Alignment.Center)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            text = text,
            style = MaterialTheme.typography.caption,
            color = contentColor
        )
    }
}