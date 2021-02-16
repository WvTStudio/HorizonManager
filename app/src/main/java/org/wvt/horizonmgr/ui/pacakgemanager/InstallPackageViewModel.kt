package org.wvt.horizonmgr.ui.pacakgemanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.webapi.NetworkException

private const val TAG = "InstallPackageVM"

class InstallPackageViewModel(
    dependencies: DependenciesContainer
) : ViewModel() {
    val packRepository = dependencies.packRepository
    val horizonMgr = dependencies.horizonManager
    val webApi = dependencies.webapi

    sealed class State {
        object Loading : State()
        object Succeed : State()
        class Error(val message: String) : State()
    }

    val state: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    val packages: MutableStateFlow<List<ChoosePackageItem>> = MutableStateFlow(emptyList())

    fun getPackages() {
        viewModelScope.launch {
            val packs = try {
                packRepository.getAllPackages()
            } catch (e: NetworkException) {
                state.value = State.Error("网络错误，请稍后再试")
                return@launch
            } catch (e: Exception) {
                Log.d(TAG, "获取分包列表失败", e)
                state.value = State.Error("未知错误，请稍后再试")
                return@launch
            }

            packages.value = packs.map {
                val manifestStr = it.getManifest()
                val manifest = horizonMgr.parsePackageManifest(manifestStr)
                ChoosePackageItem(manifest.packName, manifest.packVersionName, it.isSuggested)
            }
        }
/*
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
        }*/
    }
}