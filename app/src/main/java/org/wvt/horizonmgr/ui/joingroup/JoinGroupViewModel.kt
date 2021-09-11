package org.wvt.horizonmgr.ui.joingroup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.webapi.mgrinfo.MgrInfoModule
import javax.inject.Inject

private const val TAG = "JoinGroupVMLogger"

@HiltViewModel
class JoinGroupViewModel @Inject constructor(
    private val mgrInfo: MgrInfoModule
) : ViewModel() {

    val groups = MutableStateFlow<List<QQGroupEntry>>(emptyList())
    val isLoading = MutableStateFlow(true)
    val loadError = MutableStateFlow<Exception?>(null)
    val startQQError = MutableStateFlow(false)

    fun refresh() {
        viewModelScope.launch {
            isLoading.emit(true)
            loadError.emit(null)

            val data = try {
                withContext(Dispatchers.IO) {
                    mgrInfo.getQQGroupList().map {
                        QQGroupEntry(it.status, it.avatarUrl, it.name, it.description, it.urlLink)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取群组失败", e)
                loadError.emit(e)
                return@launch
            }
            groups.emit(data)
            isLoading.emit(false)
        }
    }

    fun handledError() {
        viewModelScope.launch {
            loadError.emit(null)
            startQQError.emit(false)
        }
    }

    fun startQQFailed() {
        viewModelScope.launch {
            startQQError.emit(true)
        }
    }
}