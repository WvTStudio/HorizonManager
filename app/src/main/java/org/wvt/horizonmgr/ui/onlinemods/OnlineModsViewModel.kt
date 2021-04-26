package org.wvt.horizonmgr.ui.onlinemods

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.consumeAsFlow
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.mod.ZipMod
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.mod.ChineseMod
import org.wvt.horizonmgr.webapi.mod.OfficialMirrorMod
import java.util.*

private const val TAG = "OnlineModsVM"

class OnlineModsViewModel(
    dependencies: DependenciesContainer
) : ViewModel() {
    private val chineseModRepository = dependencies.chineseModRepository
    private val cdnModRepository = dependencies.mirrorModRepository
    private val modDownloader = dependencies.modDownloader
    private val localCache = dependencies.localCache
    private val manager = dependencies.manager

    private var cachedMirrorMods: List<OfficialMirrorMod> = emptyList()
    val cdnMods = MutableStateFlow<List<OfficialMirrorModModel>>(emptyList())
    private var cachedChineseMods: List<ChineseMod> = emptyList()
    val chineseMods = MutableStateFlow<List<ChineseModModel>>(emptyList())

    val state = MutableStateFlow<State>(State.Loading)
    val installState = MutableStateFlow<ProgressDialogState?>(null)
    val isRefreshing = MutableStateFlow(false)

    private var installJob: Job? = null

    sealed class State {
        object Loading : State()
        object Succeed : State()
        data class Error(val message: String) : State()
    }

    // TODO: 2021/2/27 两个仓库应该有不同的排序方式

    enum class MirrorSortMode {
        DEFAULT,
        TIME_ASC, TIME_DSC,
        NAME_ASC, NAME_DSC,
        FAVORITE_ASC, FAVORITE_DSC,
        UPDATE_TIME_ASC, UPDATE_TIME_DSC
    }

    enum class ChineseSortMode {
        DEFAULT,
        TIME_ASC, TIME_DSC,
        NAME_ASC, NAME_DSC,
        DOWNLOAD_ASC, DOWNLOAD_DSC
    }

    enum class Repository {
        OfficialMirror, Chinese
    }

    val selectedRepository = MutableStateFlow<Repository>(Repository.OfficialMirror)
    val selectedCNSortMode = MutableStateFlow(ChineseSortMode.DEFAULT)
    val selectedMirrorSortMode = MutableStateFlow(MirrorSortMode.DEFAULT)
    val filterText = MutableStateFlow("")

    fun setFilterText(text: String) {
        viewModelScope.launch {
            filterText.emit(text)
            refresh()
        }
    }

    fun setSelectedRepository(repository: Repository) {
        viewModelScope.launch {
            selectedRepository.emit(repository)
            load()
        }
    }

    fun setSelectedCNSortMode(sortMode: ChineseSortMode) {
        viewModelScope.launch {
            selectedCNSortMode.emit(sortMode)
            refresh()
        }
    }

    fun setSelectedMirrorSortMode(sortMode: MirrorSortMode) {
        viewModelScope.launch {
            selectedMirrorSortMode.emit(sortMode)
            refresh()
        }
    }

    fun installChineseMod(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                installState.emit(ProgressDialogState.Loading("正在下载"))
                val pkg = localCache.getSelectedPackageUUID()?.let { uuid ->
                    manager.getInstalledPackage(uuid)
                }
                if (pkg == null) {
                    installState.emit(ProgressDialogState.Failed("安装失败", "您还未选择分包"))
                    return@launch
                }
                val selectedMod =
                    cachedChineseMods.find { it.id == id }
                        ?: return@launch // TODO: 2021/2/28 考虑显示错误信息
                val downloadLink = selectedMod.getDownloadURL()
                val task = modDownloader.downloadMod(downloadLink.fileName, downloadLink.url)
                val receiveTask = launch {
                    task.progressChannel().consumeAsFlow().conflate().collect {
                        installState.emit(ProgressDialogState.ProgressLoading("正在下载", it))
                        delay(200)
                    }
                }
                val modFile = task.await()
                receiveTask.cancelAndJoin()
                pkg.installMod(ZipMod.fromFile(modFile))
                installState.emit(ProgressDialogState.Finished("安装完成"))
            } catch (e: Exception) {
                installState.emit(ProgressDialogState.Failed("安装失败", e.message ?: "未知错误"))
            }
        }
    }

    fun installMirrorMod(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                installState.emit(ProgressDialogState.Loading("正在下载"))
                val pkg = localCache.getSelectedPackageUUID()?.let { uuid ->
                    manager.getInstalledPackage(uuid)
                }
                if (pkg == null) {
                    installState.emit(ProgressDialogState.Failed("安装失败", "您还未选择分包"))
                    return@launch
                }
                val selectedMod =
                    cachedMirrorMods.find { it.id == id }
                        ?: return@launch // TODO: 2021/2/28 考虑显示错误信息
                val url = selectedMod.downloadUrl
                val task = modDownloader.downloadMod(selectedMod.versionName, url)
                val receiveTask = launch {
                    task.progressChannel().consumeAsFlow().conflate().collect {
                        installState.emit(ProgressDialogState.ProgressLoading("正在下载", it))
                        delay(200)
                    }
                }
                val modFile = task.await()
                receiveTask.cancelAndJoin()
                pkg.installMod(ZipMod.fromFile(modFile))
                installState.emit(ProgressDialogState.Finished("安装完成"))
            } catch (e: Exception) {
                installState.emit(ProgressDialogState.Failed("安装失败", e.message ?: "未知错误"))
            }
        }
    }

    private var initialized = false

    fun init() {
        if (!initialized) {
            initialized = true
            load()
        }
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            state.emit(State.Loading)
            try {
                if (selectedRepository.value == Repository.OfficialMirror) {
                    loadMirrorMods(selectedMirrorSortMode.value, filterText.value)
                } else {
                    loadChineseMods(selectedCNSortMode.value, filterText.value)
                }
            } catch (e: NetworkException) {
                state.emit(State.Error("网络错误，请稍后重试"))
                return@launch
            } catch (e: Exception) {
                state.emit(State.Error("未知错误，请稍后重试"))
                Log.e(TAG, "获取 Mod 列表失败", e)
                return@launch
            }
            state.emit(State.Succeed)
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            isRefreshing.emit(true)
            try {
                if (selectedRepository.value == Repository.OfficialMirror) {
                    loadMirrorMods(selectedMirrorSortMode.value, filterText.value)
                } else {
                    loadChineseMods(selectedCNSortMode.value, filterText.value)
                }
            } catch (e: NetworkException) {
                state.emit(State.Error("网络错误，请稍后重试"))
                return@launch
            } catch (e: Exception) {
                state.emit(State.Error("未知错误，请稍后重试"))
                Log.e(TAG, "获取 Mod 列表失败", e)
                return@launch
            }
            isRefreshing.emit(false)
            state.emit(State.Succeed)
        }
    }

    fun cancelInstall() {
        viewModelScope.launch {
            installJob?.cancelAndJoin()
            installState.emit(null)
        }
    }

    fun installFinish() {
        viewModelScope.launch {
            installState.emit(null)
        }
    }

    private suspend fun loadMirrorMods(sortMode: MirrorSortMode, filterText: String?) =
        withContext(Dispatchers.Default) {
            val mods = withContext(Dispatchers.IO) { cdnModRepository.getAllMods() }
            cachedMirrorMods = mods
            var processed = mods.sorted(sortMode)
            if (filterText != null) {
                processed = processed.filter {
                    it.title.toLowerCase(Locale.ROOT)
                        .contains(filterText.toLowerCase(Locale.ROOT)) ||
                            it.description.toLowerCase(Locale.ROOT)
                                .contains(filterText.toLowerCase(Locale.ROOT))
                }
            }
            val mapped = processed.map {
                OfficialMirrorModModel(
                    id = it.id,
                    name = it.title,
                    description = it.description,
                    iconUrl = it.iconUrl,
                    versionName = it.versionName,
                    horizonOptimized = it.horizonOptimized,
                    lastUpdateTime = it.lastUpdate,
                    multiplayer = it.multiplayer,
                    likes = it.likes,
                    dislikes = it.dislikes
                )
            }
            cdnMods.emit(mapped)
        }

    private suspend fun loadChineseMods(sortMode: ChineseSortMode, filterText: String?) =
        withContext(Dispatchers.Default) {
            val mods = withContext(Dispatchers.IO) { chineseModRepository.getAllMods() }
            cachedChineseMods = mods
            var processed = mods.sorted(sortMode)
            if (filterText != null) {
                processed = processed.filter {
                    it.name.toLowerCase(Locale.ROOT)
                        .contains(filterText.toLowerCase(Locale.ROOT)) ||
                            it.description.toLowerCase(Locale.ROOT)
                                .contains(filterText.toLowerCase(Locale.ROOT))
                }
            }
            val mapped = processed.map {
                ChineseModModel(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    iconUrl = it.icon,
                    previewPictureURLs = it.pictures,
                    version = it.versionName,
                    time = it.time,
                    downloads = it.downloads
                )
            }
            chineseMods.emit(mapped)
        }

    private fun List<OfficialMirrorMod>.sorted(mode: MirrorSortMode): List<OfficialMirrorMod> {
        return when (mode) {
            MirrorSortMode.DEFAULT -> this // 服务端没有支持推荐排序，默认为最新模组
            MirrorSortMode.TIME_ASC -> sortedByDescending { it.id } // ID 越大代表发布越晚
            MirrorSortMode.TIME_DSC -> sortedBy { it.id }
            MirrorSortMode.NAME_ASC -> sortedBy { it.title }
            MirrorSortMode.NAME_DSC -> sortedByDescending { it.title }
            MirrorSortMode.FAVORITE_ASC -> sortedByDescending { it.likes }
            MirrorSortMode.FAVORITE_DSC -> sortedBy { it.likes }
            MirrorSortMode.UPDATE_TIME_ASC -> sortedByDescending {
                // TODO: 2021/2/28 将字符串转换成准确的时间后再排序
                it.lastUpdate
            }
            MirrorSortMode.UPDATE_TIME_DSC -> sortedBy { it.lastUpdate }
        }
    }

    private fun List<ChineseMod>.sorted(mode: ChineseSortMode): List<ChineseMod> {
        return when (mode) {
            ChineseSortMode.DEFAULT -> this // 服务端没有支持推荐排序，默认为最新模组
            ChineseSortMode.TIME_ASC -> sortedByDescending { it.id } // ID 越大代表发布越晚
            ChineseSortMode.TIME_DSC -> sortedBy { it.id }
            ChineseSortMode.NAME_ASC -> sortedBy { it.name }
            ChineseSortMode.NAME_DSC -> sortedByDescending { it.name }
            ChineseSortMode.DOWNLOAD_ASC -> sortedByDescending { it.downloads }
            ChineseSortMode.DOWNLOAD_DSC -> sortedBy { it.downloads }
        }
    }
}