package org.wvt.horizonmgr.ui.pacakgemanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.hzpack.recommendDescription
import org.wvt.horizonmgr.service.utils.calcSize
import org.wvt.horizonmgr.utils.longSizeToString
import java.text.SimpleDateFormat
import java.util.*

class PackageDetailViewModel(
    dependenciesContainer: DependenciesContainer
) : ViewModel() {
    private val manager = dependenciesContainer.manager

    val info = MutableStateFlow<PackageInformation?>(null)
    var pkgSize = MutableStateFlow<PackageSize>(PackageSize.Loading)

    sealed class PackageSize {
        object Loading : PackageSize()
        class Succeed(
            val size: Long,
            val count: Long
        ) : PackageSize() {
            val sizeStr: String = longSizeToString(size)
        }

        class Failed(val e: Exception) : PackageSize()
    }

    data class PackageInformation(
        val packageUUID: String,
        val packageName: String,
        val description: String,
        val developer: String,
        val version: String,
        val versionCode: String,
        val game: String,
        val gameVersion: String,
        val customName: String,
        val installUUID: String,
        val installDir: String,
        val installTime: String
    )

    val state = MutableStateFlow<State>(State.LOADING)

    enum class State {
        LOADING, FAILED, SUCCEED
    }

    private var packageUUID: String? = null

    fun setPackageUUID(uuid: String) {
        packageUUID = uuid
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            packageUUID?.let { pkgId ->
                state.emit(State.LOADING)
                pkgSize.emit(PackageSize.Loading)
                val pkg = manager.getInstalledPackages()
                    .find { it.getInstallationInfo().internalId == pkgId }
                if (pkg == null) {
                    // TODO: 2021/2/22 添加错误信息
                    return@launch
                }
                val manifest = pkg.getManifest()
                val installationInfo = pkg.getInstallationInfo()
                val formatter = SimpleDateFormat.getDateInstance()
                val information = PackageInformation(
                    customName = installationInfo.customName ?: "Undefined",
                    packageName = manifest.pack,
                    description = manifest.recommendDescription(),
                    developer = manifest.developer,
                    version = manifest.packVersion,
                    versionCode = manifest.packVersionCode.toString(),
                    game = manifest.game,
                    gameVersion = manifest.gameVersion,
                    installDir = pkg.packageDirectory.absolutePath,
                    installUUID = installationInfo.internalId,
                    installTime = formatter.format(Date(installationInfo.timeStamp)),
                    packageUUID = installationInfo.packageId
                )
                info.emit(information)
                state.emit(State.SUCCEED)
                val result = pkg.packageDirectory.calcSize()
                pkgSize.emit(PackageSize.Succeed(result.totalSize, result.fileCount))
            }
        }
    }
}