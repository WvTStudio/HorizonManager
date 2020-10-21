package org.wvt.horizonmgr.ui.joingroup

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.ui.WebAPIAmbient
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.theme.HorizonManagerTheme

@Composable
fun JoinGroup(onClose: () -> Unit, onGroupSelect: (url: WebAPI.QQGroupEntry) -> Unit) {
    var groupList by remember { mutableStateOf<List<WebAPI.QQGroupEntry>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val webApi = WebAPIAmbient.current

    onActive {
        scope.launch {
            try {
                groupList = webApi.getQQGroupList()
            } catch (e: Exception) {
            }
        }
    }

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
            current = groupList
        ) { groupList ->
            if (groupList.isEmpty()) {
                Stack(Modifier.fillMaxSize()) {
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
        NetworkImage(
            modifier = Modifier.padding(start = 16.dp, end = 24.dp)
                .size(42.dp)
                .clip(RoundedCornerShape(percent = 50)),
            url = avatarUrl
        )
        Column(Modifier.weight(1f)) {
            Row {
                ProvideEmphasis(emphasis = emphasis.high) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = groupName, style = MaterialTheme.typography.h6
                    )
                }
                StatusTag(
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                    text = status
                )
            }
            ProvideEmphasis(emphasis = emphasis.medium) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = description, style = MaterialTheme.typography.body1
                )
            }
        }
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
    Stack(modifier.border(1.dp, contentColor, RoundedCornerShape(4.dp))) {
        Text(
            modifier = Modifier.align(Alignment.Center)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            text = text,
            style = MaterialTheme.typography.caption,
            color = contentColor
        )
    }
}

@Composable
@Preview
private fun GroupItem() {
    HorizonManagerTheme {
        Surface {
            GroupItem(
                onClicked = {},
                avatarUrl = "",
                groupName = "Horizon 管理器内测群",
                description = "Some description",
                status = "付费"
            )
        }
    }
}

@Composable
@Preview
private fun StatusTagPreview() {
    HorizonManagerTheme {
        Surface {
            StatusTag(text = "推荐", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
@Preview
private fun JoinGroupPreview() {

}