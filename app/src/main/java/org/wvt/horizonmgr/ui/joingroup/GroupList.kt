package org.wvt.horizonmgr.ui.joingroup

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.theme.PreviewTheme

data class QQGroupEntry(
    val tag: String,
    val avatarUrl: String,
    val name: String,
    val description: String,
    val url: String
)

@Composable
internal fun GroupList(
    groups: List<QQGroupEntry>,
    onGroupSelect: (url: QQGroupEntry) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(groups) {
            GroupItem(
                onClicked = { onGroupSelect(it) },
                avatarUrl = it.avatarUrl,
                groupName = it.name,
                description = it.description,
                tag = it.tag
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
        Modifier
            .clickable(onClick = onClicked)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 24.dp)
                .wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group Avatar
            NetworkImage(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(percent = 50)),
                url = avatarUrl,
                contentDescription = stringResource(R.string.group_screen_avatar_desc)
            )
        }
        // Information
        Column(
            Modifier
                .weight(1f)
                .wrapContentHeight()
        ) {
            // GroupName
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                Box(Modifier.fillMaxWidth()) {
                    // GroupName
                    Text(
                        text = buildAnnotatedString {
                            append(groupName)
                            appendInlineContent("tag", "TAG")
                        },
                        inlineContent = mapOf("tag" to InlineTextContent(
                            Placeholder(54.sp, 26.sp, PlaceholderVerticalAlign.TextCenter)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                                StatusTag(modifier = Modifier.wrapContentSize(), text = tag)
                            }
                        })
                    )
                }
            }
            // Description
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = description, style = MaterialTheme.typography.body2
                )
            }
        }
        // Arrow Icon
        Column(
            Modifier
                .padding(start = 16.dp, end = 16.dp)
                .wrapContentSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Rounded.ArrowForward, stringResource(R.string.group_screen_action_join))
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
            modifier = Modifier
                .align(Alignment.Center)
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