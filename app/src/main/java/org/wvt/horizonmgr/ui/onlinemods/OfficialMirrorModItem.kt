package org.wvt.horizonmgr.ui.onlinemods

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.components.ModIcon
import org.wvt.horizonmgr.ui.components.loadUrlImage
import org.wvt.horizonmgr.ui.theme.PreviewTheme

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

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun OfficialMirrorModItem(
    modifier: Modifier,
    model: OfficialMirrorModModel,
    onInstallClick: () -> Unit,
    onClick: () -> Unit
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
                    modifier = Modifier.size(80.dp),
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
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = stringResource(R.string.olmod_screen_icon_likes)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = model.likes.toString(),
                            style = MaterialTheme.typography.caption
                        )
                        if (model.lastUpdateTime != null) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                modifier = Modifier.size(18.dp),
                                imageVector = Icons.Rounded.AccessTime,
                                contentDescription = stringResource(R.string.olmod_screen_icon_last_update)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = model.lastUpdateTime,
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }
                }
                // Install Button
                IconButton(onClick = onInstallClick) {
                    Icon(
                        imageVector = Icons.Rounded.Extension,
                        contentDescription = stringResource(R.string.olmod_screen_action_install),
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        OfficialMirrorModItem(modifier = Modifier.fillMaxWidth(),
            model = OfficialMirrorModModel(
                10,
                "Example",
                "Example",
                "",
                "1.0.0",
                false,
                lastUpdateTime = "1 hour ago",
                false,
                100,
                0
            ), onInstallClick = { /*TODO*/ }, onClick = {}
        )
    }
}