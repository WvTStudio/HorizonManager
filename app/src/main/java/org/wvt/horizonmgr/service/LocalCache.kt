package org.wvt.horizonmgr.service

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class LocalCache private constructor(context: Context) {
    private val fixedFoldersPref: SharedPreferences
    private val userInfoPref: SharedPreferences

    companion object {
        private const val USER_INFO = "user_info"
        private const val FIXED_FOLDERS = "fixed_folders"

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
}