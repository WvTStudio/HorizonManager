package org.wvt.horizonmgr.service.respack

import org.wvt.horizonmgr.service.CoroutineZip
import org.wvt.horizonmgr.service.ProgressDeferred
import org.wvt.horizonmgr.service.utils.translateToValidFile
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

    fun install(resourcePack: ZipResourcePackage): ProgressDeferred<Float, Unit> {
        val directoryName = resourcePack.getManifest().header.name
        val outDir = directory.resolve(directoryName).translateToValidFile()
        return CoroutineZip.unzip(resourcePack.file, outDir = outDir, autoUnbox = true)
    }
}