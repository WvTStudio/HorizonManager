package org.wvt.horizonmgr.ui.onlinemod

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import kotlin.coroutines.EmptyCoroutineContext

class OnlineViewModel(
    dependencies: DependenciesContainer
) : ViewModel() {
    private val webApi = dependencies.webapi
    private val horizonMgr = dependencies.horizonManager
    private val scope = CoroutineScope(EmptyCoroutineContext)
    private var selectedUUID: String? = null

    sealed class ListState {
        object NotLogin : ListState()
        object Loading : ListState()
        class Error(val message: String) : ListState()
        data class OK(
            val modList: List<WebAPI.OnlineModInfo>
        ) : ListState()
    }

    data class Options(
        val selectedSource: Source,
        val selectedSortMode: SortMode,
        val filterValue: String
    )

    private val _listState: MutableStateFlow<ListState> = MutableStateFlow(ListState.Loading)
    val listState: StateFlow<ListState> = _listState

    private val _options = MutableStateFlow(
        Options(Source.OFFICIAL, SortMode.DEFAULT, "")
    )
    val options: StateFlow<Options> = _options


    val downloadState: MutableStateFlow<ProgressDialogState?> = MutableStateFlow(null)
    val installState: MutableStateFlow<ProgressDialogState?> = MutableStateFlow(null)

    val sources = Source.values().toList()
    val sortModes = SortMode.values().toList()

    /*
    var selectedSource = MutableStateFlow(Source.OFFICIAL)
    var sortMode = MutableStateFlow(SortMode.DEFAULT)
    var filterValue = MutableStateFlow("")*/

    init {
        refresh()
    }

    enum class Source(val label: String) {
        OFFICIAL("官方源"), CN("汉化组源")
    }

    enum class SortMode(val label: String) {
        DEFAULT("推荐排序"), TIME_ASC("最新发布"), TIME_DSC("最先发布"),
        NAME_ASC("名称排序"), NAME_DSC("名称倒序")
    }

    private var unfilteredList: List<WebAPI.OnlineModInfo> = emptyList()

    fun setSelectedSource(source: Source) {
        _options.value = _options.value.copy(selectedSource = source)
        refresh()
        loadFilter()
    }

    fun setSelectedSortMode(sortMode: SortMode) {
        _options.value = _options.value.copy(selectedSortMode = sortMode)
        loadFilter()
    }

    fun setFilterValue(filterValue: String) {
        _options.value = _options.value.copy(filterValue = filterValue)
        loadFilter()
    }

    fun refresh() {
        scope.launch(Dispatchers.IO) {
            _listState.value = ListState.Loading
            val mods = try {
                (if (options.value.selectedSource == Source.OFFICIAL) {
                    webApi.getModsFromOfficial()
                } else webApi.getModsFromCN())
            } catch (e: WebAPI.WebAPIException) {
                return@launch
            }
            unfilteredList = mods
            val sorted = mods.sorted(options.value.selectedSortMode)
            _listState.value = ListState.OK(sorted)
        }
    }

    private fun loadFilter() {
        scope.launch {
            val filtered = unfilteredList.sorted(options.value.selectedSortMode).let {
                val filterValue = _options.value.filterValue
                if (filterValue.isNotBlank()) it.filter {
                    it.name.contains(filterValue) || it.description.contains(filterValue)
                } else it
            }
            _listState.value = ListState.OK(filtered)
        }
    }

    fun download(mod: WebAPI.OnlineModInfo) {
        scope.launch {
            downloadState.value = ProgressDialogState.ProgressLoading("正在下载", 0f)
            try {
                val task = webApi.downloadMod(mod)
                val receiveTask = launch {
                    task.progressChannel().consumeAsFlow().conflate().collect {
                        downloadState.value = ProgressDialogState.ProgressLoading("正在下载", it)
                        delay(200)
                    }
                }
                task.await()
                receiveTask.cancel()
                downloadState.value = ProgressDialogState.Finished("下载完成")
            } catch (e: Exception) {
                e.printStackTrace()
                downloadState.value =
                    ProgressDialogState.Failed(
                        "下载失败",
                        e.localizedMessage ?: ""
                    )
                return@launch
            }
        }
    }

    fun downloadFinish() {
        downloadState.value = null
    }

    fun setSelectedUUID(str: String?) {
        selectedUUID = str
    }

    fun install(mod: WebAPI.OnlineModInfo) {
        scope.launch {
            try {
                installState.value = ProgressDialogState.ProgressLoading("正在下载", 0f)
                val task = webApi.downloadMod(mod)
                val receiveTask = launch {
                    task.progressChannel().consumeAsFlow().conflate().collect {
                        installState.value = ProgressDialogState.ProgressLoading("正在下载", it)
                        delay(200)
                    }
                }
                val modFile = task.await()
                receiveTask.cancel()

                installState.value = ProgressDialogState.Loading("正在安装")
                horizonMgr.installMod(selectedUUID ?: error("Package not specified!"), modFile)
            } catch (e: Exception) {
                e.printStackTrace()
                installState.value = ProgressDialogState.Failed(
                    "安装失败",
                    e.localizedMessage ?: ""
                )
                return@launch
            }
            installState.value = ProgressDialogState.Finished("安装成功")
        }
    }

    fun installFinish() {
        installState.value = null
    }


    private fun List<WebAPI.OnlineModInfo>.sorted(mode: SortMode): List<WebAPI.OnlineModInfo> {
        return when (mode) {
            SortMode.DEFAULT -> sortedBy { it.index } // 服务端没有支持推荐排序，默认为最新模组
            SortMode.TIME_ASC -> sortedByDescending { it.id } // ID 越大代表
            SortMode.TIME_DSC -> sortedBy { it.id }
            SortMode.NAME_ASC -> sortedBy { it.name }
            SortMode.NAME_DSC -> sortedByDescending { it.name }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}