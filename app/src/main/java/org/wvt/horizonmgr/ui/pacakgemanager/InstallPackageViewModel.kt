package org.wvt.horizonmgr.ui.pacakgemanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer

class InstallPackageViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {
    private val webApi = dependencies.webapi
    private val horizonMgr = dependencies.horizonManager

    sealed class State {
        object Loading : State()
        object Succeed : State()
        class Error(val err: Throwable) : State()
    }

    val state: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    val packages: MutableStateFlow<List<ChoosePackageItem>> = MutableStateFlow(emptyList())

    private fun getPackages() {
        // TODO: 2020/11/5
        viewModelScope.launch {
            val pkgs = try {
                webApi.getPackages()
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO 显示错误提示和重试选项
                return@launch
            }
            val mappedPackages = pkgs.map {
                val manifest = horizonMgr.parsePackageManifest(it.manifestStr)
                ChoosePackageItem(
                    manifest.packName,
                    manifest.packVersionName,
                    it.recommended
                )
            }
            packages.value = mappedPackages
        }
    }
}