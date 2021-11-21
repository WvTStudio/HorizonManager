package org.wvt.horizonmgr.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import org.wvt.horizonmgr.service.hzpack.HorizonManager
import org.wvt.horizonmgr.service.hzpack.PackageManifest
import org.wvt.horizonmgr.service.hzpack.PackageManifestWrapper
import org.wvt.horizonmgr.service.hzpack.ZipPackage
import org.wvt.horizonmgr.ui.onlineinstall.ChoosePackageItem
import org.wvt.horizonmgr.utils.OfficialCDNPackageDownloader
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.pack.OfficialCDNPackage
import org.wvt.horizonmgr.webapi.pack.OfficialPackageCDNRepository
import javax.inject.Inject

private const val TAG = "InstallPackageVM"

@HiltViewModel
class InstallPackageViewModel @Inject constructor(
    private val packRepository: OfficialPackageCDNRepository,
    private val downloader: OfficialCDNPackageDownloader,
    private val mgr: HorizonManager
) : ViewModel() {

    sealed class StepState {
        object Waiting : StepState()
        class Running(val progress: androidx.compose.runtime.State<Float>) : StepState()
        class Error(val message: String) : StepState()
        object Complete : StepState()
    }

    data class DownloadStep(
        val chunk: Int,
        val state: androidx.compose.runtime.State<State>
    ) {
        sealed class State {
            object Waiting : State()
            class Running(val progress: androidx.compose.runtime.State<Long>, val total: Long) :
                State()

            class Error(val message: String) : State()
            object Complete : State()
        }
    }

    sealed class State {
        object Loading : State()
        object Succeed : State()
        class Error(val message: String) : State()
    }

    val getPackageState = MutableStateFlow<State>(State.Loading)
    val packages = MutableStateFlow<List<ChoosePackageItem>>(emptyList())

    val selectedPackageManifest = MutableStateFlow<PackageManifest?>(null)

    private var packs = emptyList<OfficialCDNPackage>()
    private var selectedPackage: OfficialCDNPackage? = null
    private var customName: String? = null

    fun getPackages() {
        viewModelScope.launch(Dispatchers.IO) {
            getPackageState.emit(State.Loading)
            packs = try {
                packRepository.getAllPackages()
            } catch (e: NetworkException) {
                getPackageState.emit(State.Error("网络错误，请稍后再试"))
                return@launch
            } catch (e: Exception) {
                Log.e(TAG, "获取分包列表失败", e)
                getPackageState.emit(State.Error("未知错误，请稍后再试"))
                return@launch
            }

            val mappedPacks = packs.map {
                try {
                    val manifestStr = it.getManifest()
                    val manifest = PackageManifestWrapper.fromJson(manifestStr)
                    ChoosePackageItem(
                        it.uuid,
                        manifest.pack,
                        manifest.packVersion,
                        it.isSuggested
                    )
                } catch (e: NetworkException) {
                    getPackageState.emit(State.Error("获取分包清单时出现网络错误"))
                    return@launch
                } catch (e: Exception) {
                    Log.e(TAG, "获取分包清单失败", e)
                    getPackageState.emit(State.Error("未知错误，请稍后再试"))
                    return@launch
                }
            }

            packages.emit(mappedPacks)
            getPackageState.emit(State.Succeed)
        }
    }

    fun selectPackage(uuid: String) {
        val selectedPackage = packs.find { it.uuid == uuid }
        selectedPackageManifest.value = null
        this.selectedPackage = selectedPackage
        if (selectedPackage != null) viewModelScope.launch {
            val manifestStr = try {
                selectedPackage.getManifest()
            } catch (e: Exception) {
                Log.e(TAG, "获取分包清单失败", e)
                return@launch
            }

            val manifest = try {
                PackageManifestWrapper.fromJson(manifestStr)
            } catch (e: Exception) {
                Log.e(TAG, "解析分包清单失败", e)
                return@launch
            }
            selectedPackageManifest.value = manifest
        }
    }

    fun setCustomName(name: String) {
        customName = name
    }

    private var installJob: Job? = null

    var totalProgress = MutableStateFlow(0f)
    val mergeState = MutableStateFlow<StepState>(StepState.Waiting)
    val installState = MutableStateFlow<StepState>(StepState.Waiting)

    fun startInstall() {
        viewModelScope.launch(Dispatchers.IO) {
            val pack = selectedPackage ?: return@launch
            val downloaded = mutableStateOf(0L)
            val downloadState = mutableStateOf<DownloadStep.State>(DownloadStep.State.Waiting)
            downloadSteps.emit(listOf(DownloadStep(0, downloadState)))

            // TODO: Code cleanup
            val task = downloader.download(pack)
            var sentTotalSize = false
            val job = launch {
                task.progress.collect { state ->
                    val progress = state.first.toDouble() / state.second.toDouble()
                    downloaded.value = state.first
                    if (!sentTotalSize && state.second > 0L) {
                        downloadState.value = DownloadStep.State.Running(downloaded, state.second)
                        sentTotalSize = true
                    }
                    totalProgress.emit(progress.toFloat() / 2f)
                    delay(200)
                }
            }

            val result = try {
                task.await()
            } catch (e: Exception) {
                Log.e(TAG, "下载分包失败", e)
                // TODO: 2021/2/20 添加显示
                downloadState.value = DownloadStep.State.Error(e.message ?: "Unknown Error")
                return@launch
            } finally {
                job.cancel()
            }

            downloadState.value = DownloadStep.State.Complete

            mergeState.emit(StepState.Complete)
            installState.emit(StepState.Running(mutableStateOf(0f)))

            // Download succeed, install this package.
            try {
                mgr.installPackage(
                    ZipPackage(result.packageZipFile),
                    graphicsZip = result.graphicsFile,
                    pack.uuid,
                    customName
                )
            } catch (e: Exception) {
                Log.e(TAG, "安装分包失败", e)
                installState.emit(StepState.Error(e.message ?: "Unknown Error"))
                return@launch
            }

            delay(500)
            totalProgress.emit(1f)
            installState.emit(StepState.Complete)
        }
    }

    fun cancelInstall() {
        viewModelScope.launch {
            installJob?.cancelAndJoin()
        }
        // TODO: 2021/2/20 添加取消安装功能
    }

    val downloadSteps = MutableStateFlow<List<DownloadStep>>(emptyList())
}