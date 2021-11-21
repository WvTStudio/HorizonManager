package org.wvt.horizonmgr.ui.fileselector

import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.utils.LocalCache
import java.io.File
import javax.inject.Inject

private const val TAG = "FileSelectorVM"

@HiltViewModel
class FileSelectorViewModel @Inject constructor(
    private val localCache: LocalCache
) : ViewModel() {

    sealed class State {
        object Loading : State()
        object Succeed : State()
        sealed class Error : State() {
            object CannotReadFolder : Error()
            class Other(val e: Throwable?) : Error()
        }
    }

    val state = MutableStateFlow<State>(State.Loading)

    val currentPathDepth = MutableStateFlow<Int>(0)

    val pathTabs = MutableStateFlow<List<String>>(emptyList())
    val favoriteFolders = MutableStateFlow<List<String>>(emptyList())
    val listFiles = MutableStateFlow<List<PathListEntry>>(emptyList())

    private var rootDepth = 0
    private var mFavoriteFolders: List<File> = emptyList()
    private var mTabList: List<File> = emptyList()
    private var mListFiles: List<File> = emptyList()


    fun init() {
        viewModelScope.launch(Dispatchers.Default) {
            state.emit(State.Loading)
            try {
                loadFavoriteFolders()
            } catch (e: Exception) {
                // TODO: 2021/3/4
                state.emit(State.Error.Other(e))
                return@launch
            }
            try {
                enterRootFolder()
            } catch (e: CannotReadFolderException) {
                Log.e(TAG, "Cannot read folder: ${e.path}")
                state.emit(State.Error.CannotReadFolder)
                return@launch
            } catch (e: CannotGetFilesException) {
                Log.e(TAG, "Cannot get files in ${e.path}")
                state.emit(State.Error.Other(null))
                return@launch
            } catch (e: Exception) {
                return@launch
            }
            state.emit(State.Succeed)
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.Default) {
            state.emit(State.Loading)
            val file = mTabList[currentPathDepth.value]
            try {
                parsePaths(file)
                enterFolder(file)
            } catch (e: CannotReadFolderException) {
                Log.e(TAG, "Cannot read folder: ${e.path}")
                state.emit(State.Error.CannotReadFolder)
                return@launch
            } catch (e: CannotGetFilesException) {
                Log.e(TAG, "Cannot get files in ${e.path}")
                state.emit(State.Error.Other(null))
                return@launch
            } catch (e: Exception) {
                return@launch
            }
            state.emit(State.Error.CannotReadFolder)
        }
    }

    fun selectPathDepth(depth: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            state.emit(State.Loading)
            val file = mTabList[depth]
            currentPathDepth.emit(depth)
            try {
                enterFolder(file)
            } catch (e: CannotReadFolderException) {
                Log.e(TAG, "Cannot read folder: ${e.path}")
                state.emit(State.Error.CannotReadFolder)
                return@launch
            } catch (e: CannotGetFilesException) {
                Log.e(TAG, "Cannot get files in ${e.path}")
                state.emit(State.Error.Other(null))
                return@launch
            } catch (e: Exception) {
                return@launch
            }
            state.emit(State.Succeed)
        }
    }

    fun enterFavoriteFolder(index: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            state.emit(State.Loading)
            val file = mFavoriteFolders[index]
            try {
                parsePaths(file)
                enterFolder(File(file.path))
            } catch (e: CannotReadFolderException) {
                Log.e(TAG, "Cannot read folder: ${e.path}")
                state.emit(State.Error.CannotReadFolder)
                return@launch
            } catch (e: CannotGetFilesException) {
                Log.e(TAG, "Cannot get files in ${e.path}")
                state.emit(State.Error.Other(null))
                return@launch
            } catch (e: Exception) {
                return@launch
            }
            state.emit(State.Succeed)
        }
    }

    fun enterListFolder(index: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            state.emit(State.Loading)
            val file = mListFiles[index]
            try {
                parsePaths(file)
                enterFolder(file)
            } catch (e: CannotReadFolderException) {
                Log.e(TAG, "Cannot read folder: ${e.path}")
                state.emit(State.Error.CannotReadFolder)
                return@launch
            } catch (e: CannotGetFilesException) {
                Log.e(TAG, "Cannot get files in ${e.path}")
                state.emit(State.Error.Other(null))
                return@launch
            } catch (e: Exception) {
                return@launch
            }
            state.emit(State.Succeed)
        }
    }

    fun getFileAbsolutePath(index: Int): String {
        return mListFiles[index].absolutePath
    }

    fun star(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = mListFiles[index]
            localCache.addFixedFolder(file.name, file.absolutePath)
            loadFavoriteFolders()
        }
    }

    fun unStar(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = mListFiles[index]
            localCache.removeFixedFolder(file.absolutePath)
            loadFavoriteFolders()
        }
    }

    fun pinnedUnStar(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = mFavoriteFolders[index]
            localCache.removeFixedFolder(file.absolutePath)
            mFavoriteFolders = mFavoriteFolders.toMutableList().also { it.remove(file) }
            loadFavoriteFolders()
        }
    }

    private suspend fun loadFavoriteFolders() {
        val fixedFolders = withContext(Dispatchers.IO) { localCache.getFixedFolders() }
        mFavoriteFolders = fixedFolders.map { File(it.path) }
        favoriteFolders.emit(mFavoriteFolders.map { it.name })
    }

    private suspend fun enterRootFolder() {
        val dir = Environment.getExternalStorageDirectory()
        val parts = dir.parts()
        rootDepth = parts.size
        parsePaths(dir)
        enterFolder(dir)
    }

    private suspend fun parsePaths(folder: File) {
        val parts = folder.absoluteFile.normalize().parts()
        val filteredParts = parts.subList(rootDepth - 1, parts.size)

        mTabList = filteredParts.map { it.second }
        pathTabs.emit(filteredParts.map { it.first })
        currentPathDepth.emit(filteredParts.size - 1)
    }

    class CannotReadFolderException(val path: String) : Exception()
    class CannotGetFilesException(val path: String) : Exception()

    private suspend fun enterFolder(folder: File) {
        if (!folder.canRead()) {
            throw CannotReadFolderException(folder.absolutePath)
        }
        val files = folder.listFiles() ?: throw CannotReadFolderException(folder.absolutePath)
        val sorted = files.asSequence()
            .sortedBy { it.name.lowercase() }
            .sortedByDescending { it.isDirectory }
            .toList()
        mListFiles = sorted
        listFiles.emit(sorted.map {
            if (it.isDirectory) {
                PathListEntry.Folder(it.name)
            } else {
                PathListEntry.File(it.name)
            }
        })
    }

    private fun File.parts(): List<Pair<String, File>> {
        val parts = getParts(normalize().absolutePath)
        return parts.map {
            val file = File(it)
            file.name to file
        }
    }

    private fun getParts(path: String): List<String> {
        val result = mutableListOf<String>()

        val startsWithSeparator = path.startsWith(File.separatorChar)
        val endsWithSeparator = path.endsWith(File.separatorChar)

        val startIndex = if (startsWithSeparator) 1 else 0
        val endIndex = if (endsWithSeparator) path.length - 1 else path.length

        for (i in startIndex until endIndex) {
            if (path[i] == File.separatorChar) {
                val part = path.substring(0, i)
                result.add(part)
            }
        }

        result.add(path.substring(0, endIndex))
        return result
    }
}