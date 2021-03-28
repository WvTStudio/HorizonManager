package org.wvt.horizonmgr.service.level

import java.io.File

class MCLevel internal constructor(val directory: File) {
    class NotMCLevelException(
        val directory: File,
        val missingFiles: List<String>
    ) : Exception(
        "${directory.absolutePath} is not a level directory. Missing ${missingFiles.size} files: ${
            missingFiles.joinToString(
                prefix = "[",
                postfix = "]"
            )
        }"
    )

    companion object {
        @Throws(NotMCLevelException::class)
        fun parseByDirectory(directory: File): MCLevel {
            if (!isMCLevel(directory)) throw NotMCLevelException(directory, essentialFiles.toList())
            return MCLevel(directory)
        }

        private val essentialFiles = setOf("levelname.txt", "level.dat", "db")

        fun isMCLevel(directory: File): Boolean {
            if (!directory.isDirectory) return false
            val listFiles = directory.listFiles() ?: return false
            essentialFiles.forEach {
                if (listFiles.find { file -> file.name == it } == null) return false
            }
            return true
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