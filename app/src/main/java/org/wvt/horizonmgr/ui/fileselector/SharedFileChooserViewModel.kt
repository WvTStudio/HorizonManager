package org.wvt.horizonmgr.ui.fileselector

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SharedFileChooserVM"

@Singleton
class SharedFileChooserViewModel @Inject constructor() : ViewModel() {
    init {
        Log.d(TAG, "init: ${this.hashCode()}")
    }

    private val _selected: MutableStateFlow<SelectData?> = MutableStateFlow(null)
    val selected = _selected.asStateFlow()

    private var requestCode: String? = null

    data class SelectData(val requestCode: String?, val path: String)

    fun setSelected(path: String) {
        _selected.value = SelectData(requestCode, path)
    }

    fun setRequestCode(code: String) {
        requestCode = code
    }

    fun handledSelectedFile() {
        requestCode = null
        _selected.value = null
    }
}