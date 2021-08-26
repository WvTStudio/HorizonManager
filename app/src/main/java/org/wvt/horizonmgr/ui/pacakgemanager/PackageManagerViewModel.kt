package org.wvt.horizonmgr.ui.pacakgemanager

import android.os.Environment
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import org.wvt.horizonmgr.service.hzpack.*
import org.wvt.horizonmgr.ui.components.InputDialogHostState
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import org.wvt.horizonmgr.utils.LocalCache
import org.wvt.horizonmgr.webapi.pack.OfficialCDNPackage
import org.wvt.horizonmgr.webapi.pack.OfficialPackageCDNRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "PackageManagerVM"

@HiltViewModel
class PackageManagerViewModel @Inject constructor(
    private val mgr: HorizonManager,
    private val localCache: LocalCache,
    private val packageCDNRepository: OfficialPackageCDNRepository
) : ViewModel() {
    private val _packages = MutableStateFlow(emptyList<PackageManagerItem>())
    private val _progressState = MutableStateFlow<ProgressDialogState?>(null)
    private val dateFormatter = SimpleDateFormat.getDateInstance()
    private var cachedPackages: List<InstalledPackage> = emptyList()

    val state = MutableStateFlow<State>(State.Initializing)
    val errors = MutableStateFlow<List<String>>(emptyList())
    val isRefreshing = MutableStateFlow(false)
    val progressState: StateFlow<ProgressDialogState?> = _progressState.asStateFlow()
    val packages: StateFlow<List<PackageManagerItem>> = _packages.asStateFlow()
    val selectedPackage = MutableStateFlow<String?>(null)

    sealed class State {
        object Initializing : State()
        class Error(val message: String, val detail: String?) : State()
        object OK : State()
    }

    fun loadPackages() {
        viewModelScope.launch(Dispatchers.IO) {
            if (state.value == State.Initializing) {
                loadData()
                loadSelectedPackage()
                launch { checkUpdate() }
            } else {
                isRefreshing.emit(true)
                loadData()
                loadSelectedPackage()
                launch { checkUpdate() }
                isRefreshing.emit(false)
            }
        }
    }

    private suspend fun loadData() {
        val getResult = try {
            mgr.getInstalledPackages()
        } catch (e: Exception) {
            Log.e(TAG, "获取分包失败", e)
            state.emit(State.Error("获取分包失败", e.message))
            return
        }
        Log.d(TAG, "获取到 ${getResult.packages.size} 个分包")
        val mappedExceptions = getResult.errors.map {
            "${it.file}: ${it.error.message ?: "未知错误"}"
        }.toMutableList()
        val result = getResult.packages.mapNotNull {
            val installInfo: InstallationInfo
            val manifest: PackageManifest

            try {
                installInfo = it.getInstallationInfo()
                manifest = it.getManifest()
            } catch (e: Exception) {
                mappedExceptions.add("${it.packageDirectory}: ${e.message ?: "未知错误"}")
                Log.d(TAG, "分包解析失败", e)
                return@mapNotNull null
            }
            PackageManagerItem(
                uuid = installInfo.internalId,
                name = installInfo.customName ?: manifest.pack,
                timeStr = dateFormatter.format(Date(installInfo.timeStamp)),
                description = manifest.recommendDescription()
            )
        }
        cachedPackages = getResult.packages
        delay(200)
        _packages.emit(result)
        errors.emit(mappedExceptions)


        state.emit(State.OK)
    }

    private suspend fun loadSelectedPackage() {
        val uuid = localCache.getSelectedPackageUUID()
        val selectedPackage = uuid?.let { uuid ->
            cachedPackages.find {
                it.getInstallationInfo().internalId == uuid
            }
        }
        if (selectedPackage != null) {
            this.selectedPackage.emit(uuid)
        } else {
            this.selectedPackage.emit(null)
        }
    }

    fun selectPackage(uuid: String?) {
        viewModelScope.launch {
            localCache.setSelectedPackageUUID(uuid)
            selectedPackage.emit(uuid)
        }
    }

    fun deletePackage(
        uuid: String,
        confirmDeleteDialogHostState: ConfirmDeleteDialogHostState
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (confirmDeleteDialogHostState.showDialog() ==
                ConfirmDeleteDialogHostState.DialogResult.CONFIRM
            ) {
                _progressState.emit(ProgressDialogState.Loading("正在删除"))
                try {
                    cachedPackages.find { it.getInstallationInfo().internalId == uuid }?.delete()
                } catch (e: Exception) {
                    Log.e(TAG, "删除分包失败", e)
                    _progressState.emit(
                        ProgressDialogState.Failed("删除失败", e.localizedMessage ?: "")
                    )
                    return@launch
                }
                _progressState.emit(ProgressDialogState.Finished("删除成功"))
                localCache.setSelectedPackageUUID(null)
                selectedPackage.emit(null)
                loadPackages()
            }
        }
    }

    fun renamePackage(uuid: String, inputDialogHostState: InputDialogHostState) {
        viewModelScope.launch(Dispatchers.IO) {
            val pkg =
                cachedPackages.find { it.getInstallationInfo().internalId == uuid } ?: return@launch
            val (manifest, installationInfo) = try {
                pkg.getManifest() to pkg.getInstallationInfo()
            } catch (e: Exception) {
                _progressState.emit(
                    ProgressDialogState.Failed("获取分包信息失败", e.localizedMessage ?: "")
                )
                return@launch
            }

            val result =
                inputDialogHostState.showDialog(
                    installationInfo.customName ?: manifest.pack,
                    "重命名",
                    "请输入新名称"
                )
            if (result is InputDialogHostState.DialogResult.Confirm) {
                _progressState.emit(ProgressDialogState.Loading("正在重命名"))
                try {
                    pkg.rename(result.input)
                } catch (e: Exception) {
                    Log.e(TAG, "重命名分包失败", e)
                    _progressState.emit(
                        ProgressDialogState.Failed("重命名失败", e.localizedMessage ?: "")
                    )
                    return@launch
                }
                _progressState.emit(ProgressDialogState.Finished("重命名完成"))
                loadPackages()
            }
        }
    }

    fun clonePackage(uuid: String, inputDialogHostState: InputDialogHostState) {
        viewModelScope.launch(Dispatchers.IO) {
            val pkg =
                cachedPackages.find { it.getInstallationInfo().internalId == uuid } ?: return@launch
            val (manifest, installationInfo) = try {
                pkg.getManifest() to pkg.getInstallationInfo()
            } catch (e: Exception) {
                _progressState.emit(
                    ProgressDialogState.Failed("获取分包信息失败", e.localizedMessage ?: "")
                )
                return@launch
            }
            val result = inputDialogHostState.showDialog(
                installationInfo.customName ?: manifest.pack,
                "克隆",
                "请输入新名称"
            )
            if (result is InputDialogHostState.DialogResult.Confirm) {
                _progressState.emit(ProgressDialogState.Loading("正在克隆"))
                try {
                    pkg.clone(result.input)
                } catch (e: Exception) {
                    Log.e(TAG, "克隆分包失败", e)
                    _progressState.emit(
                        ProgressDialogState.Failed("克隆失败", e.localizedMessage ?: "")
                    )
                    return@launch
                }
                _progressState.emit(ProgressDialogState.Finished("克隆完成"))
                loadPackages()
            }
        }
    }

    fun dismiss() {
        viewModelScope.launch {
            _progressState.emit(null)
        }
    }

    fun selectedFile(filePath: String) {
        // TODO: 2021/2/20 实现选择文件安装
        viewModelScope.launch {
            _progressState.emit(ProgressDialogState.Loading("正在安装"))
            val zipPackage = withContext(Dispatchers.IO) {
                ZipPackage(File(filePath))
            }
            if (!zipPackage.isZipPackage()) {
                _progressState.emit(ProgressDialogState.Failed("解析失败", "您选择的文件可能不是一个正确的分包"))
                return@launch
            }
            try {
                // TODO: 2021/5/27 允许自定义一些配置
                withContext(Dispatchers.IO) {
                    mgr.installPackage(
                        ZipPackage(File(filePath)),
                        null,
                        null,
                        null
                    )
                }
            } catch (e: Exception) {
                _progressState.emit(ProgressDialogState.Failed("安装失败", "安装过程中出现错误"))
                return@launch
            }
            _progressState.emit(ProgressDialogState.Finished("安装完成"))
            loadPackages()
        }
    }


    val updatablePackages: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())

    val checkingUpdateState: MutableStateFlow<CheckingUpdateState> =
        MutableStateFlow(CheckingUpdateState.Init)

    sealed class CheckingUpdateState {
        object Init : CheckingUpdateState()
        object Checking : CheckingUpdateState()
        object Succeed : CheckingUpdateState()
        data class Failed(val message: String) : CheckingUpdateState()
    }

    private suspend fun checkUpdate() {
        checkingUpdateState.emit(CheckingUpdateState.Checking)
        val updatable = mutableSetOf<String>()

        val latestPackages = try {
            packageCDNRepository.getAllPackages().map {
                it to PackageManifest.fromJson(it.getManifest())
            }
        } catch (e: Exception) {
            checkingUpdateState.emit(CheckingUpdateState.Failed("在线获取分包失败"))
            return
        }

        cachedPackages.forEach { installed ->
            try {
                latestPackages.forEach { latest ->
                    val installationInfo = installed.getInstallationInfo()

                    if (installationInfo.packageId == latest.first.uuid) {
                        val manifest = installed.getManifest()
                        if (manifest.packVersionCode < latest.second.packVersionCode) {
                            updatable.add(installationInfo.internalId)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "checkUpdate", e)
            }
        }

        updatablePackages.emit(updatable)
        checkingUpdateState.emit(CheckingUpdateState.Succeed)
    }


    var updateState by mutableStateOf<UpdateState?>(null)
        private set

    sealed class UpdateState {
        object Parsing : UpdateState()
        data class Downloading(val progress: androidx.compose.runtime.State<Float>) : UpdateState()
        object Installing : UpdateState()
        object Succeed : UpdateState()
        object Failed : UpdateState()
    }

    fun updatePackage(uuid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateState = UpdateState.Parsing
            val pkg = cachedPackages.find {
                it.getInstallationInfo().internalId == uuid
            } ?: run {
                updateState = UpdateState.Failed
                return@launch
            }
            val pkgId = pkg.getInstallationInfo().packageId
            val onlinePkg = packageCDNRepository.getAllPackages().find {
                it.uuid == pkgId
            } ?: run {
                updateState = UpdateState.Failed
                return@launch
            }
            val progress = mutableStateOf(0f)
            updateState = UpdateState.Downloading(progress)
            delay(1000)
            // TODO
            updateState = UpdateState.Installing
            delay(1000)
            updateState = UpdateState.Succeed
        }
    }

    var sharePackageState by mutableStateOf<SharePackageState?>(null)
        private set

    sealed class SharePackageState {
        object EditName : SharePackageState()
        object Processing : SharePackageState()
        data class Succeed(val file: File) : SharePackageState()
        object Failed : SharePackageState()
    }

    fun sharePackage(uuid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sharePackageState = SharePackageState.EditName
            val name = waitForShareName()
            val pkg = mgr.getInstalledPackage(uuid) ?: return@launch

            val outputFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).resolve("$name.zip")

            sharePackageState = SharePackageState.Processing
            try {
                ZipFile(outputFile).addFolder(pkg.packageDirectory)
            } catch (e: Exception) {
                sharePackageState = SharePackageState.Failed
                e.printStackTrace()
                return@launch
            }
            sharePackageState = SharePackageState.Succeed(outputFile)
        }
    }

    private var cont: Continuation<String>? = null

    private suspend fun waitForShareName(): String {
        return suspendCoroutine<String> {
            cont = it
        }
    }

    fun setShareName(name: String) {
        cont?.resume(name)
    }
}