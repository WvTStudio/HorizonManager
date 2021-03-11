package org.wvt.horizonmgr.ui.onlinemods

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.components.ModIcon
import org.wvt.horizonmgr.ui.components.loadUrlImage

@Stable
data class OfficialMirrorModModel(
    val id: Int,
    val name: String,
    val description: String,
    val iconUrl: String,
    val versionName: String,
    val horizonOptimized: Boolean,
    val lastUpdateTime: String?,
    val multiplayer: Boolean,
    val likes: Int,
    val dislikes: Int,
)

@Composable
internal fun OfficialMirrorModItem(
    modifier: Modifier,
    model: OfficialMirrorModModel,
    onInstallClick: () -> Unit,
    onClick: () -> Unit
) {
    val modIcon by loadUrlImage(url = model.iconUrl)

    Card(modifier = modifier, elevation = 1.dp) {
        Column(
            Modifier
                .clickable(onClick = onClick)
                .padding(top = 16.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
        ) {
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
                    if (model.versionName.isNotBlank()) {
                        Text(
                            modifier = Modifier.padding(top = 2.dp),
                            text = model.versionName, style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
                        )
                    }
                    // Secondary Text
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = model.description, style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
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
                            imageVector = Icons.Default.Favorite, contentDescription = "Likes"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = model.likes.toString(), style = MaterialTheme.typography.caption)
                        if (model.lastUpdateTime != null) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                modifier = Modifier.size(18.dp),
                                imageVector = Icons.Default.AccessTime, contentDescription = "Last update"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = model.lastUpdateTime, style = MaterialTheme.typography.caption)
                        }
                    }
                }
                // Install Button
                IconButton(onClick = onInstallClick) {
                    Icon(imageVector = Icons.Filled.Extension, contentDescription = "安装", tint = MaterialTheme.colors.primary)
                }
            }
        }
    }
}