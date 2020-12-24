package org.wvt.horizonmgr.ui.pacakgemanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.legacyservice.HorizonManager
import org.wvt.horizonmgr.utils.calcSize
import org.wvt.horizonmgr.utils.longSizeToString
import java.io.File

class PackageDetailViewModel(
    dependenciesContainer: DependenciesContainer
) : ViewModel() {
    private val hzmgr = dependenciesContainer.horizonManager

    val pkgInfo = MutableStateFlow<HorizonManager.LocalPackage?>(null)
    val manifest = MutableStateFlow<HorizonManager.PackageManifest?>(null)
    var pkgSize = MutableStateFlow<PackageSize>(PackageSize.Loading)

    sealed class PackageSize {
        object Loading : PackageSize()
        class Succeed(val sizeStr: String, val size: Long, val count: Long) : PackageSize()
        class Failed(val e: Exception) : PackageSize()
    }

    val state = MutableStateFlow<State>(State.LOADING)

    enum class State {
        LOADING, FAILED, SUCCEED
    }

    fun refresh(pkgId: String) {
        viewModelScope.launch(Dispatchers.Default) {
            state.value = State.LOADING
            val mPkgInfo: HorizonManager.LocalPackage
            val mManifest: HorizonManager.PackageManifest
            try {
                mPkgInfo = hzmgr.getPackageInfo(pkgId)!!
                mManifest = hzmgr.parsePackageManifest(mPkgInfo.manifest)
            } catch (e: Exception) {
                state.value = State.FAILED
                return@launch
            }
            pkgInfo.value = mPkgInfo
            manifest.value = mManifest
            state.value = State.SUCCEED

            val (count, totalSize) = try {
                File(mPkgInfo.path).also { println(it) }.calcSize().also { println(it) }
            } catch (e: Exception) {
                e.printStackTrace()
                pkgSize.value = PackageSize.Failed(e)
                return@launch
            }

            val sizeStr = try {
                longSizeToString(totalSize)
            } catch (e: Exception) {
                pkgSize.value = PackageSize.Failed(e)
                return@launch
            }

            pkgSize.value = PackageSize.Succeed(sizeStr, totalSize, count)
        }
    }
}