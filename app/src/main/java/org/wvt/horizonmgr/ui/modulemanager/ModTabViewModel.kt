package org.wvt.horizonmgr.ui.modulemanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.legacyservice.HorizonManager
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import java.util.*

class ModTabViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {
    private val horizonMgr = dependencies.horizonManager
    private var selectedPackageUUID: String? = null

    private val _progressState = MutableStateFlow<ProgressDialogState?>(null)
    val progressState: StateFlow<ProgressDialogState?> = _progressState

    data class ModEntry(
        val id: String,
        val name: String,
        val description: String,
        val iconPath: String?
    )

    private val _mods = MutableStateFlow(emptyList<ModEntry>())
    val mods: StateFlow<List<ModEntry>> = _mods

    private val _enabledMods = MutableStateFlow(mutableSetOf<String>())
    val enabledMods: StateFlow<Set<String>> = _enabledMods

    private var modMap: Map<String, HorizonManager.InstalledModInfo> = mutableMapOf()

    sealed class State {
        object Loading : State()
        object PackageNotSelected : State()
        object OK : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state

    private var mItems: MutableList<HorizonManager.InstalledModInfo> = mutableListOf()

    fun setSelectedUUID(uuid: String?) {
        selectedPackageUUID = uuid
    }

    fun load() {
        viewModelScope.launch {
            selectedPackageUUID?.let {
                mItems = try {
                    horizonMgr.getMods(it).toMutableList()
                } catch (e: HorizonManager.PackageNotFoundException) {
                    _state.value = State.PackageNotSelected
                    return@launch
                }
                val map = mutableMapOf<String, HorizonManager.InstalledModInfo>()
                val enabled = mutableSetOf<String>()
                val result = mutableListOf<ModEntry>()
                mItems.forEachIndexed { index, item ->
                    if (item.enable) enabled.add(item.path)
                    result.add(ModEntry(item.path, item.name, item.description, item.iconPath))
                    map[item.path] = item
                }
                modMap = map
                _enabledMods.value = enabled
                _mods.value = result
                _state.value = State.OK
            } ?: run {
                _state.value = State.PackageNotSelected
                return@launch
            }
        }
    }

    fun enableMod(mod: ModEntry) {
        viewModelScope.launch {
            val realMod = modMap[mod.id] ?: error("没有此 Mod")
            horizonMgr.enableModByPath(realMod.path)

            _enabledMods.value = _enabledMods.value.toMutableSet().also {
                if (!it.contains(mod.id)) {
                    it.add(mod.id)
                }
            }
        }
    }

    fun disableMod(mod: ModEntry) {
        viewModelScope.launch {
            val realMod = modMap[mod.id] ?: error("没有此 Mod")
            horizonMgr.disableModByPath(realMod.path)

            _enabledMods.value = _enabledMods.value.toMutableSet().also { it.remove(mod.id) }
        }
    }

    fun deleteMod(mod: ModEntry) {
        viewModelScope.launch {
            _progressState.value = ProgressDialogState.Loading("正在删除")

            try {
                val realMod = modMap[mod.id] ?: error("没有此 Mod")
                horizonMgr.deleteModByPath(realMod.path)
            } catch (e: Exception) {
                e.printStackTrace()
                _progressState.value =
                    ProgressDialogState.Failed("删除失败", e.localizedMessage)
                return@launch
            }
            _progressState.value = ProgressDialogState.Finished("删除成功")
            load()
        }
    }

    fun dismiss() {
        _progressState.value = null
    }
}