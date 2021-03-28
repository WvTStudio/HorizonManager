package org.wvt.horizonmgr.service.level

import org.wvt.horizonmgr.service.CoroutineZip
import org.wvt.horizonmgr.service.ProgressDeferred
import org.wvt.horizonmgr.service.utils.translateToValidFile
import java.io.File

class MCLevelManager(private val worldsDir: File) {

    data class GetResult(
        val levels: List<MCLevel>,
        val errors: List<ErrorEntry>
    )

    data class ErrorEntry(
        val file: File,
        val error: Throwable
    )

    /**
     * 获取 [worldsDir] 的存档
     * @return 获取的结果，[levels] 将确保为正确的存档，[errors] 存有解析时发生的所有错误，可能是 IO 错误，也可能是存档格式不正确
     */
    fun getLevels(): GetResult {
        if (!worldsDir.exists()) return GetResult(emptyList(), emptyList())
        val dirs = worldsDir.listFiles() ?: return GetResult(emptyList(), emptyList())

        val result = mutableListOf<MCLevel>()
        val errors = mutableListOf<ErrorEntry>()

        for (file in dirs) {
            if (!file.isDirectory) continue

            val level = try {
                MCLevel.parseByDirectory(file)
            } catch (e: Exception) {
                errors.add(ErrorEntry(file, e))
                continue
            }

            result.add(level)
        }

        return GetResult(result, errors)
    }

    fun installLevel(zipMCLevel: ZipMCLevel): ProgressDeferred<Float, Unit> {
        val outDir = worldsDir.resolve(zipMCLevel.getLevelName()).translateToValidFile()
        return CoroutineZip.unzip(zipMCLevel.file, outDir, false)
    }
}