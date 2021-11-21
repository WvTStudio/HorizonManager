package org.wvt.horizonmgr.viewmodel

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.service.hzpack.HorizonManager
import org.wvt.horizonmgr.service.hzpack.InstalledPackage
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import org.wvt.horizonmgr.utils.LocalCache
import org.wvt.horizonmgr.utils.ModDownloader
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DMViewModel @Inject constructor(
    private val localCache: LocalCache,
    private val downloader: ModDownloader,
    private val manager: HorizonManager
) : ViewModel() {

    private val _mods = MutableStateFlow(emptyList<DownloadedMod>())
    private val _progressState = MutableStateFlow<ProgressDialogState?>(null)
    private var map: Map<DownloadedMod, ModDownloader.DownloadedMod> = emptyMap()

    val mods: StateFlow<List<DownloadedMod>> = _mods
    val progressState: StateFlow<ProgressDialogState?> = _progressState
    val isRefreshing = MutableStateFlow(false)

    data class DownloadedMod(
        internal val uuid: String,
        val name: String,
        val description: String,
        val icon: ImageBitmap?
    )

    private suspend fun getSelectedPackage(): InstalledPackage? {
        val uuid = localCache.getSelectedPackageUUID() ?: return null
        return manager.getInstalledPackage(uuid)
    }

    private var initialized = false

    fun init() {
        if (!initialized) {
            initialized = true
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val mMap = LinkedHashMap<DownloadedMod, ModDownloader.DownloadedMod>()
            downloader.getDownloadedMods().forEach {
                val modInfo = it.zipMod.getModInfo()
                val icon = try {
                    BitmapFactory.decodeStream(it.zipMod.getModIconStream()).asImageBitmap()
                } catch (e: Exception) {
                    null
                }
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
                val pack = getSelectedPackage() ?: return@launch
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