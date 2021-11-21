package org.wvt.horizonmgr.ui.onlinemods

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.GetApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.components.ModIcon
import org.wvt.horizonmgr.ui.components.loadUrlImage
import org.wvt.horizonmgr.ui.theme.PreviewTheme

data class ChineseModModel(
    val id: Int,
    val name: String,
    val description: String,
    val iconUrl: String,
    val previewPictureURLs: List<String>,
    val version: String,
    val time: String,
    val downloads: Int
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ChineseModItem(
    modifier: Modifier,
    model: ChineseModModel,
    onClick: () -> Unit,
    onInstallClick: () -> Unit,
) {
    val modIcon by loadUrlImage(url = model.iconUrl)
    Card(modifier = modifier, elevation = 1.dp, onClick = onClick) {
        Column(Modifier.padding(top = 16.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)) {
            // Information
            Row {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    // Title
                    Text(model.name, style = MaterialTheme.typography.h5)
                    // Version
                    if (model.version.isNotBlank()) {
                        Text(
                            modifier = Modifier.padding(top = 2.dp),
                            text = model.version, style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
                        )
                    }
                    // Secondary Text
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = model.description, style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
                // Mod Icon
                ModIcon(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    image = modIcon
                )
            }
            // Actions
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(Modifier.weight(1f)) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = Icons.Rounded.GetApp, contentDescription = "Downloads"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = model.downloads.toString(),
                            style = MaterialTheme.typography.caption
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = "Last update"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = model.time, style = MaterialTheme.typography.caption)
                    }
                }
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.primary) {
                    // Install Button
                    IconButton(onClick = onInstallClick) {
                        Icon(Icons.Rounded.Extension, contentDescription = "安装")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        ChineseModItem(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            model = ChineseModModel(1, "Example", "Example", "", listOf(""), "1.0.0", "2020-2-2", 100),
            onClick = { },
            onInstallClick = { }
        )
    }
}