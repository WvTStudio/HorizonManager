package org.wvt.horizonmgr.ui.downloadlist

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.wvt.horizonmgr.DependenciesContainer

class DownloadListViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {
    private val _downloadItems = MutableStateFlow(emptyList<String>())
    val downloadItems: StateFlow<List<String>> = _downloadItems
}