package org.wvt.horizonmgr.ui.downloaded

import androidx.compose.ui.graphics.ImageAsset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import java.io.File

class DMViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {

    data class DownloadedMod(
        internal val uuid: String,
        val name: String,
        val description: String,
        val icon: ImageAsset?
    )

    private val _mods = MutableStateFlow(emptyList<DownloadedMod>())
    val mods: StateFlow<List<DownloadedMod>> = _mods

    private var selectedUUID: String? = null

    private val _progressState = MutableStateFlow<ProgressDialogState?>(null)
    val progressState: StateFlow<ProgressDialogState?> = _progressState

    init {
        refresh()
    }

    fun setSelectedPackage(uuid: String?) {
        selectedUUID = uuid
    }

    private var map: Map<DownloadedMod, HorizonManager.UninstalledModInfo> = emptyMap()

    fun refresh() {
        viewModelScope.launch {
            val result = mutableListOf<DownloadedMod>()
            val m = mutableMapOf<DownloadedMod, HorizonManager.UninstalledModInfo>()
            dependencies.horizonManager.getDownloadedMods().forEach {
                val k = DownloadedMod(
                    it.path,
                    it.name,
                    it.description,
                    null
                )
                result.add(k)
                m[k] = it
            }
            map = m
            _mods.value = result
        }
    }

    fun install(dm: DownloadedMod) {
        viewModelScope.launch {
            try {
                _progressState.value = ProgressDialogState.Loading("正在安装")
                selectedUUID?.let { pkg ->
                    map[dm]?.let { mod ->
                        dependencies.horizonManager.installMod(pkg, File(mod.path))
                        _progressState.value = ProgressDialogState.Finished("安装成功")
                    }
                }
            } catch (e: Exception) {
                _progressState.value = ProgressDialogState.Failed("安装失败", e.message ?: " ")
            }
        }
    }

    fun delete(dm: DownloadedMod) {
        viewModelScope.launch {
            map[dm]?.let { mod ->
                File(mod.path).delete()
                refresh()
            }
        }
    }

    fun dismiss() {
        _progressState.value = null
    }
}