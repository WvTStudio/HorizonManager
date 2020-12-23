package org.wvt.horizonmgr.service

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalCache private constructor(context: Context) {
    private val fixedFoldersPref: SharedPreferences
    private val userInfoPref: SharedPreferences
    private val selectedPackagePref: SharedPreferences
    private val optionsPref: SharedPreferences

    companion object {
        private const val USER_INFO = "user_info"
        private const val FIXED_FOLDERS = "fixed_folders"
        private const val SELECTED_PACKAGE = "selected_package"
        private const val OPTIONS = "options"

        private var instance: LocalCache? = null

        fun getInstance(): LocalCache {
            return instance!!
        }

        fun createInstance(context: Context): LocalCache {
            instance = LocalCache(context)
            return instance!!
        }
    }

    init {
        fixedFoldersPref = context.getSharedPreferences(FIXED_FOLDERS, Context.MODE_PRIVATE)
        userInfoPref = context.getSharedPreferences(USER_INFO, Context.MODE_PRIVATE)
        selectedPackagePref = context.getSharedPreferences(SELECTED_PACKAGE, Context.MODE_PRIVATE)
        optionsPref = context.getSharedPreferences(OPTIONS, Context.MODE_PRIVATE)
    }

    data class CachedUserInfo(
        val id: Int,
        val name: String,
        val account: String,
        val avatarUrl: String
    )

    suspend fun getCachedUserInfo(): CachedUserInfo? = withContext(Dispatchers.IO) {
        with(userInfoPref) {
            val name = getString("name", null) ?: return@with null
            val account = getString("account", null) ?: return@with null
            val avatarUrl = getString("avatar_url", null) ?: return@with null
            val id = getInt("id", -1).takeIf { it != -1 } ?: return@with null
            CachedUserInfo(id, name, account, avatarUrl)
        }
    }

    suspend fun clearCachedUserInfo() = withContext(Dispatchers.IO) {
        userInfoPref.edit {
            clear()
        }
    }

    suspend fun cacheUserInfo(id: Int, name: String, account: String, avatarUrl: String) {
        withContext(Dispatchers.IO) {
            userInfoPref.edit {
                putInt("id", id)
                putString("account", account)
                putString("avatar_url", avatarUrl)
                putString("name", name)
            }
        }
    }

    suspend fun cacheUserInfo(userInfo: CachedUserInfo) {
        withContext(Dispatchers.IO) {
            userInfoPref.edit {
                putInt("id", userInfo.id)
                putString("account", userInfo.account)
                putString("avatar_url", userInfo.avatarUrl)
                putString("name", userInfo.name)
            }
        }
    }

    suspend fun getSelectedPackageUUID(): String? = withContext(Dispatchers.IO) {
        selectedPackagePref.getString("uuid", null)
    }

    suspend fun setSelectedPackageUUID(uuid: String?) = withContext(Dispatchers.IO) {
        selectedPackagePref.edit {
            putString("uuid", uuid)
        }
    }

    data class FixedFolder(val name: String, val path: String)

    fun getFixedFolders(): List<FixedFolder> {
        return fixedFoldersPref.all.map {
            FixedFolder(it.key, it.value.toString())
        }
    }

    fun addFixedFolder(name: String, path: String) {
        fixedFoldersPref.edit {
            putString(name, path)
        }
    }

    fun removeFixedFolder(path: String) {
        for (entry in fixedFoldersPref.all) {
            if (entry.value.toString() == path) {
                fixedFoldersPref.edit {
                    remove(entry.key)
                }
                break
            }
        }
    }

    fun getIgnoreVersion(): Int? {
        return optionsPref.getInt("ignore_version", -1).takeIf { it != -1 }
    }

    fun setIgnoreVersion(versionCode: Int) {
        optionsPref.edit {
            putInt("ignore_version", versionCode)
        }
    }

    fun clearIgnoreVersion() {
        optionsPref.edit {
            remove("ignore_version")
        }
    }
}

fun WebAPI.UserInfo.mapToCachedUserInfo() =
    LocalCache.CachedUserInfo(id, name, account, avatarUrl)
