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

    fun load(pkgId: String) {
        viewModelScope.launch {
            state.emit(State.LOADING)
            pkgSize.emit(PackageSize.Loading)
            val pkg = manager.getInstalledPackages().find { it.getInstallUUID() == pkgId }
            if (pkg == null) {
                // TODO: 2021/2/22 添加错误信息
                return@launch
            }
            val formatter = SimpleDateFormat.getDateInstance()
            val information = PackageInformation(
                customName = pkg.getCustomName() ?: "Undefined",
                packageName = pkg.getName(),
                description = pkg.getDescription()["en"] ?: "No Description",
                developer = pkg.getDeveloper(),
                version = pkg.getVersion(),
                versionCode = pkg.getVersionCode().toString(),
                game = pkg.getGame(),
                gameVersion = pkg.getGameVersion(),
                installDir = pkg.getInstallDir().absolutePath,
                installUUID = pkg.getInstallUUID(),
                installTime = formatter.format(Date(pkg.getInstallTimeStamp())),
                packageUUID = pkg.getPackageUUID()
            )
            info.emit(information)
            state.emit(State.SUCCEED)
            val (count, size) = pkg.getInstallDir().calcSize()
            pkgSize.emit(PackageSize.Succeed(size, count))
        }
    }
}