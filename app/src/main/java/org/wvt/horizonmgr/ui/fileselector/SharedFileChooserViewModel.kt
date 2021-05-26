package org.wvt.horizonmgr.ui.fileselector

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "SharedFileChooserVM"

object SharedFileChooserViewModel : ViewModel() {
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