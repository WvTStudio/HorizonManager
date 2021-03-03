package org.wvt.horizonmgr.service.level

import java.io.File

class MCLevel internal constructor(
    val directory: File
) {
    companion object {
        internal fun parseByDirectory(directory: File): MCLevel {
            return MCLevel(directory)
        }
    }

    class CannotParseLevelNameException(override val cause: Throwable) : Exception(cause)

    fun getInfo(): LevelInfo {
        val img = directory.resolve("world_icon.jpeg").takeIf { it.exists() }?.absolutePath
        val levelName = try {
            directory.resolve("levelname.txt").readText()
        } catch (e: Exception) {
            throw CannotParseLevelNameException(e)
        }
        return LevelInfo(levelName, directory.absolutePath, img)
    }

    fun rename(newName: String) {
        val levelNameFile = directory.resolve("levelname.txt")
        levelNameFile.writeText(newName)
    }

    fun delete() {
        directory.deleteRecursively()
    }
}