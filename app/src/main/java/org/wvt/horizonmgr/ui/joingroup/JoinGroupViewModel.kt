package org.wvt.horizonmgr.ui.joingroup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.WebAPI

class JoinGroupViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {
    private val _groups = MutableStateFlow<List<WebAPI.QQGroupEntry>>(emptyList())
    val groups: StateFlow<List<WebAPI.QQGroupEntry>> = _groups
    val isLoading = MutableStateFlow(true)
    val loadError = MutableStateFlow<Exception?>(null)
    val startQQError = MutableStateFlow<Exception?>(null)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading.value = true
            loadError.value = null
            try {
                _groups.value = dependencies.webapi.getQQGroupList()
            } catch (e: Exception) {
                loadError.value = e
            }
            isLoading.value = false
        }
    }

    fun joinGroup(intentUrl: String, context: Context) {
        viewModelScope.launch {
            startQQError.value = null
            val intent = Intent()
            try {
                intent.data = Uri.parse(intentUrl)
                context.startActivity(intent)
            } catch (e: Exception) {
                startQQError.value = e
            }
        }
    }
}