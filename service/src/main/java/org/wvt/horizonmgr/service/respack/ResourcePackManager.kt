package org.wvt.horizonmgr.service.respack

import org.wvt.horizonmgr.service.CoroutineZip
import org.wvt.horizonmgr.service.ProgressDeferred
import org.wvt.horizonmgr.service.utils.translateToValidFile
import java.io.File

class ResourcePackManager(val directory: File) {
    data class GetResult(
        val resPacks: List<ResourcePackage>,
        val errors: List<ErrorEntry>
    )

    data class ErrorEntry(
        val file: File,
        val error: Throwable
    )

    /**
     * 获取资源包列表
     * @return 获取的结果，[resPacks] 将确保为正确的资源包，[errors] 存有解析时发生的所有错误，可能是 IO 错误，也可能是资源包文件夹格式不正确
     */
    fun getPackages(): GetResult {
        if (!directory.exists()) return GetResult(emptyList(), emptyList())
        val dirs = directory.listFiles() ?: return GetResult(emptyList(), emptyList())

        val result = mutableListOf<ResourcePackage>()
        val errors = mutableListOf<ErrorEntry>()

        for (file in dirs) {
            if (!file.isDirectory) continue

            val resPack = try {
                ResourcePackage.parseByDirectory(file)
            } catch (e: Exception) {
                errors.add(ErrorEntry(file, e))
                continue
            }

            result.add(resPack)
        }

        return GetResult(result, errors)
    }

    /**
     * 安装一个 Zip 格式的资源包
     */
    fun install(resourcePack: ZipResourcePackage): ProgressDeferred<Float, Unit> {
        val directoryName = resourcePack.getManifest().header.name
        val outDir = directory.resolve(directoryName).translateToValidFile()
        return CoroutineZip.unzip(resourcePack.file, outDir = outDir, autoUnbox = false)
    }
}