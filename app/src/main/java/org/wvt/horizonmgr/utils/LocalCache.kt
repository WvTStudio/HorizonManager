package org.wvt.horizonmgr.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "LocalCache"
class LocalCache constructor(context: Context) {
    private val fixedFoldersPref: SharedPreferences =
        context.getSharedPreferences(FIXED_FOLDERS, Context.MODE_PRIVATE)
    private val userInfoPref: SharedPreferences = context.getSharedPreferences(USER_INFO, Context.MODE_PRIVATE)
    private val selectedPackagePref: SharedPreferences =
        context.getSharedPreferences(SELECTED_PACKAGE, Context.MODE_PRIVATE)
    private val optionsPref: SharedPreferences = context.getSharedPreferences(OPTIONS, Context.MODE_PRIVATE)

    companion object {
        private const val USER_INFO = "user_info"
        private const val FIXED_FOLDERS = "fixed_folders"
        private const val SELECTED_PACKAGE = "selected_package"
        private const val OPTIONS = "options"
    }

    data class CachedUserInfo(
        val uid: String,
        val name: String,
        val account: String,
        val avatarUrl: String
    )

    suspend fun getCachedUserInfo(): CachedUserInfo? = withContext(Dispatchers.IO) {
        with(userInfoPref) {
            val name = getString("name", null) ?: return@with null
            val account = getString("account", null) ?: return@with null
            val avatarUrl = getString("avatar_url", null) ?: return@with null
            val id = getString("uid", null) ?: return@with null
            CachedUserInfo(id, name, account, avatarUrl)
        }
    }

    suspend fun clearCachedUserInfo() = withContext(Dispatchers.IO) {
        userInfoPref.edit {
            clear()
        }
    }

    suspend fun cacheUserInfo(uid: String, name: String, account: String, avatarUrl: String) {
        withContext(Dispatchers.IO) {
            userInfoPref.edit {
                putString("uid", uid)
                putString("account", account)
                putString("avatar_url", avatarUrl)
                putString("name", name)
            }
        }
    }

    suspend fun cacheUserInfo(userInfo: CachedUserInfo) {
        withContext(Dispatchers.IO) {
            userInfoPref.edit {
                putString("uid", userInfo.uid)
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
        Log.d(TAG, "Star folder, name: $name, path: $path.")
        fixedFoldersPref.edit {
            putString(name, path)
        }
    }

    fun removeFixedFolder(path: String) {
        Log.d(TAG, "Remove star folder: $path")

        for (entry in fixedFoldersPref.all) {
            if (entry.value.toString() == path) {
                fixedFoldersPref.edit {
                    remove(entry.key)
                }
                return
            }
        }
        Log.d(TAG, "Folder is not in the favorite list.")
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