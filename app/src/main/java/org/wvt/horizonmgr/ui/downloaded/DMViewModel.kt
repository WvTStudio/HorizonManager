package org.wvt.horizonmgr.ui.downloaded

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.hzpack.InstalledPackage
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import org.wvt.horizonmgr.utils.ModDownloader
import java.io.File

class DMViewModel(dependencies: DependenciesContainer) : ViewModel() {
    private val downloader = dependencies.modDownloader
    private val manager = dependencies.manager

    data class DownloadedMod(
        internal val uuid: String,
        val name: String,
        val description: String,
        val icon: ImageBitmap?
    )

    private val _mods = MutableStateFlow(emptyList<DownloadedMod>())
    val mods: StateFlow<List<DownloadedMod>> = _mods

    private var selectedUUID: String? = null
    private var selectedPackage: InstalledPackage? = null
    private val _progressState = MutableStateFlow<ProgressDialogState?>(null)

    val progressState: StateFlow<ProgressDialogState?> = _progressState

    fun setSelectedPackage(uuid: String?) {
        viewModelScope.launch {
            selectedUUID = uuid
            selectedPackage = withContext(Dispatchers.IO) {
                manager.getInstalledPackages().find { it.getInstallationInfo().internalId == uuid }
            }
        }
    }

    private var map: Map<DownloadedMod, ModDownloader.DownloadedMod> = emptyMap()

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val mMap = LinkedHashMap<DownloadedMod, ModDownloader.DownloadedMod>()
            downloader.getDownloadedMods().forEach {
                val modInfo = it.zipMod.getModInfo()
                val icon = try {
                    BitmapFactory.decodeStream(it.zipMod.getModIconStream()).asImageBitmap()
                } catch (e: Exception) { null }
                val downloaded = DownloadedMod(
                    it.path,
                    modInfo.name,
                    modInfo.description,
                    icon
                )
                mMap[downloaded] = it
            }
            _mods.value = mMap.keys.toList()
            map = mMap
        }
    }

    fun install(dm: DownloadedMod) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _progressState.value = ProgressDialogState.Loading("正在安装")
                val mod = map[dm] ?: return@launch
                val pack = selectedPackage ?: return@launch
                pack.installMod(mod.zipMod)
                _progressState.value = ProgressDialogState.Finished("安装成功")
            } catch (e: Exception) {
                _progressState.value = ProgressDialogState.Failed("安装失败", e.message ?: " ")
            }
        }
    }

    fun delete(dm: DownloadedMod) {
        viewModelScope.launch(Dispatchers.IO) {
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