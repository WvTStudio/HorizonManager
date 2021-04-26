package org.wvt.horizonmgr.ui.onlinemods

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.components.EmptyPage

@Composable
fun ChineseModList(
    modifier: Modifier,
    mods: List<ChineseModModel>,
    onItemClick: (index: Int) -> Unit,
    onInstallClick: (index: Int) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(top = 8.dp, bottom = 64.dp)
    ) {
        if (mods.isEmpty()) item {
            EmptyPage(Modifier.fillParentMaxSize()) {
                Text("什么模组都没有")
            }
        } else itemsIndexed(mods) { index: Int, item: ChineseModModel ->
            ChineseModItem(
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