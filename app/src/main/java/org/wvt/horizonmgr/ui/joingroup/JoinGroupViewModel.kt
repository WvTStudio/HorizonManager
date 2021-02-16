package org.wvt.horizonmgr.ui.joingroup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer

private const val TAG = "JoinGroupVMLogger"

class JoinGroupViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {
    val groups = MutableStateFlow<List<QQGroupEntry>>(emptyList())
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
            val data = try {
                dependencies.mgrInfo.getQQGroupList().map {
                    QQGroupEntry(it.status, it.avatarUrl, it.name, it.description, it.urlLink)
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取群组失败", e)
                loadError.value = e
                return@launch
            }
            groups.value = data
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
                return@launch
            }
        }
    }

    fun handledError() {
        loadError.value = null
        startQQError.value = null
    }
}