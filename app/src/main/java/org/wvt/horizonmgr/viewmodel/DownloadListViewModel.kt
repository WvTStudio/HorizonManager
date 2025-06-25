package org.wvt.horizonmgr.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DownloadListViewModel : ViewModel() {
    private val _downloadItems = MutableStateFlow(emptyList<String>())
    val downloadItems: StateFlow<List<String>> = _downloadItems
}