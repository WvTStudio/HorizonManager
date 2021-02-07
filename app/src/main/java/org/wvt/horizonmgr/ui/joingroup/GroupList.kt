package org.wvt.horizonmgr.ui.joingroup

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.legacyservice.WebAPI
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@Composable
internal fun GroupList(
    groups: List<WebAPI.QQGroupEntry>,
    onGroupSelect: (url: WebAPI.QQGroupEntry) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // use `item` for separate elements like headers
        // and `items` for lists of identical elements
        items(groups) {
            GroupItem(
                onClicked = { onGroupSelect(it) },
                avatarUrl = it.avatar,
                groupName = it.name,
                description = it.description,
                tag = it.status
            )
        }
    }
}

@Composable
private fun GroupItem(
    onClicked: () -> Unit,
    avatarUrl: String,
    groupName: String,
    description: String,
    tag: String
) {
    Row(
        Modifier.clickable(onClick = onClicked).fillMaxWidth().wrapContentHeight()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 24.dp).wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group Avatar
            NetworkImage(
                modifier = Modifier.size(42.dp)
                    .clip(RoundedCornerShape(percent = 50)),
                url = avatarUrl,
                contentDescription = "头像"
            )
            // Tag
            StatusTag(modifier = Modifier.padding(top = 8.dp), text = tag)
        }
        // Information
        Column(Modifier.weight(1f).wrapContentHeight()) {
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
            Modifier.padding(start = 16.dp, end = 16.dp).wrapContentSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.ArrowForward, "加入")
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

@Preview
@Composable
private fun ItemPreview() {
    PreviewTheme {
        GroupItem(
            onClicked = {},
            avatarUrl = "",
            groupName = "Example",
            description = "Example",
            tag = "OPEN"
        )
    }
}