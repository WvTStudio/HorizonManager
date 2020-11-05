package org.wvt.horizonmgr.ui.downloadlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.onCommit
import org.wvt.horizonmgr.dependenciesViewModel

@Composable
fun DownloadList() {
    val vm = dependenciesViewModel<DownloadListViewModel>()
}

@Composable
private fun DownloadItem(
    name: String,
    total: Long,
    current: Long
) {

}