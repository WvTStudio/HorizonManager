package org.wvt.horizonmgr.service.level

import org.wvt.horizonmgr.service.CoroutineZip
import org.wvt.horizonmgr.service.ProgressDeferred
import org.wvt.horizonmgr.service.utils.translateToValidFile
import java.io.File

class MCLevelManager(private val worldsDir: File) {

    class WorldsDirectoryNotExists() : Exception()

    data class GetResult(
        val levels: List<MCLevel>,
        val errors: List<ErrorEntry>
    )

    data class ErrorEntry(
        val file: File,
        val error: Throwable
    )

    fun getLevels(): GetResult {
        if (!worldsDir.exists()) throw WorldsDirectoryNotExists()
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