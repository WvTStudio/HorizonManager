package org.wvt.horizonmgr.ui.onlinemods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.mod.ChineseMod
import org.wvt.horizonmgr.webapi.mod.OfficialCDNMod

class NewOnlineViewModel(
    dependencies: DependenciesContainer
) : ViewModel() {
    private val chineseModRepository = dependencies.chineseModRepository
    private val cdnModRepository = dependencies.cdnModRepository

    val cdnMods = MutableStateFlow<List<OfficialCDNMod>>(emptyList())
    val chineseMods = MutableStateFlow<List<ChineseMod>>(emptyList())

    fun loadCdnMods() {
        viewModelScope.launch {
            val mods = try {
                cdnModRepository.getAllMods()
            } catch (e: NetworkException) {
                // TODO: 2021/2/23
                return@launch
            }
            cdnMods.emit(mods)
        }
    }

    fun loadChineseMods() {
        viewModelScope.launch {

        }
    }
}