package org.wvt.horizonmgr.ui.downloaded

import android.graphics.BlendMode
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.ui.tooling.preview.Preview
import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.ui.HorizonManagerAmbient
import org.wvt.horizonmgr.ui.theme.HorizonManagerTheme

@Composable
fun DownloadedMods() {
    val horizonMgr = HorizonManagerAmbient.current

    var mods by remember {
        mutableStateOf(emptyList<HorizonManager.UninstalledModInfo>())
    }

    launchInComposition {
        mods = horizonMgr.getDownloadedMods()
    }

    ScrollableColumn {
        mods.fastForEach {
            ModItem(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                name = it.name,
                description = it.description,
                icon = it.path,
                onInstallClicked = {}
            )
        }
    }
}

@Composable
private fun ModItem(
    modifier: Modifier = Modifier,
    name: String, description: String, icon: String, onInstallClicked: () -> Unit
) {
    Card(modifier = modifier, elevation = 2.dp) {
        Column {
            Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
                Text(text = name, style = MaterialTheme.typography.h6)
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = description
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // InstallButton
                IconButton(
                    modifier = Modifier.padding(16.dp),
                    onClick = onInstallClicked
                ) { Icon(asset = Icons.Filled.Extension, tint = MaterialTheme.colors.primary) }
            }
        }
    }
}

private sealed class DownloadItemState {
    object Finished : DownloadItemState()
    class Error(val message: Exception) : DownloadItemState()
    class Downloading(
        val name: String,
        val progress: Float
    ) : DownloadItemState()
}

@Preview
@Composable
private fun DownloadItemPreview() {
    HorizonManagerTheme {
        ModItem(
            name = "Example", description = "Example Description", icon = "", onInstallClicked = {

            })
    }
}
