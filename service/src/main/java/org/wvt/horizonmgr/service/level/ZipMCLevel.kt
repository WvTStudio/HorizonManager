package org.wvt.horizonmgr.service.level

import java.io.File
import java.io.InputStream
import java.util.zip.ZipException
import java.util.zip.ZipFile

class ZipMCLevel(
    val file: File,
    internal val zipFile: ZipFile = ZipFile(file)
) {
    class NotZipMCLevelException() : Exception()

    companion object {
        @Throws(NotZipMCLevelException::class)
        fun parse(file: File): ZipMCLevel {
            val zip = try {
                ZipFile(file)
            } catch (e: ZipException) {
                throw NotZipMCLevelException()
            }
            if (!isZipMCLevel(zip)) throw NotZipMCLevelException()
            return ZipMCLevel(file, zip)
        }

        fun isZipMCLevel(zip: ZipFile): Boolean {
            val hasLevelName = zip.getEntry("levelname.txt") != null
            val hasWorldIcon = zip.getEntry("world_icon.jpeg") != null
            val hasLevelDat = zip.getEntry("level.dat") != null
            val hasDBDirectory = zip.getEntry("db") != null
            return hasLevelName && hasLevelDat && hasDBDirectory
        }
    }

    fun getLevelName(): String {
        return zipFile.getInputStream(zipFile.getEntry("levelname.txt")).reader().readText()
    }

    fun getScreenShot(): InputStream? {
        return zipFile.getEntry("world_icon.jpeg")?.let {
            zipFile.getInputStream(it)
        }
    }
}