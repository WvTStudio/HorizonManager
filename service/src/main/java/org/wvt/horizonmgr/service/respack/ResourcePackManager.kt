package org.wvt.horizonmgr.service.respack

import org.wvt.horizonmgr.service.utils.calcSize
import java.io.File

class ResourcePackManager(val directory: File) {
    /**
     * 获取到的列表可能包含不正确的资源包
     */
    fun getPackages(): List<ResourcePackage> {
        val result = mutableListOf<ResourcePackage>()
        val listFiles = directory.listFiles() ?: error("Cannot access")
        for (file in listFiles) {
            if (file.isDirectory) {
                result.add(ResourcePackage.parseByDirectory(file))
            }
        }
        return result
    }
}

class ResourcePackage internal constructor(val directory: File) {
    private val manifestFile = directory.resolve("manifest.json")

    companion object {
        fun parseByDirectory(dir: File): ResourcePackage {
            return ResourcePackage(dir)
        }
    }

    /**
     * 当解析失败时抛出错误
     */
    fun getManifest(): ResourcePackManifest {
        val manifestStr = manifestFile.readText()
        return ResourcePackManifest.fromJson(manifestStr)
    }

    fun getIcon(): File? {
        return directory.resolve("pack_icon.png").takeIf { it.exists() }
    }

    suspend fun calcSize(): Long {
        return directory.calcSize().fileCount
    }
}

