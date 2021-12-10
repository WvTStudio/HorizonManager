package org.wvt.horizonmgr.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.ui.main.UserInformation
import org.wvt.horizonmgr.utils.LocalCache
import javax.inject.Inject

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val localCache: LocalCache
) : ViewModel() {
    val userInfo = MutableStateFlow<UserInformation?>(null)

    fun logOut() {
        viewModelScope.launch(Dispatchers.IO) {
            localCache.clearCachedUserInfo()
            userInfo.value = null
        }
    }

    private suspend fun loadUserInfo() = withContext(Dispatchers.IO) {
        try {
            userInfo.value = localCache.getCachedUserInfo()?.let {
                UserInformation(it.name, it.account, it.avatarUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "loadUserInfo: failed", e)
        }
    }

    fun resume() {
        viewModelScope.launch {
            loadUserInfo()
        }
    }
}