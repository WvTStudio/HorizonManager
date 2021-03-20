package org.wvt.horizonmgr.ui.onlinemods

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OfficialMirrorModList(
    modifier: Modifier,
    mods: List<OfficialMirrorModModel>,
    onItemClick: (index: Int) -> Unit,
    onInstallClick: (index: Int) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(top = 8.dp, bottom = 64.dp)
    ) {
        itemsIndexed(mods) { index: Int, item: OfficialMirrorModModel ->
            OfficialMirrorModItem(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                model = item,
                onClick = { onItemClick(index) },
                onInstallClick = { onInstallClick(index) }
            )
        }
    }
}