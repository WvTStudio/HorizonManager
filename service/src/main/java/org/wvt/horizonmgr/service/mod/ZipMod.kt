package org.wvt.horizonmgr.service.mod

import java.io.File
import java.io.InputStream
import java.util.zip.ZipException
import java.util.zip.ZipFile

/**
 * 该类代表一个 Zip 形式的，还未安装的 MOD
 * 该压缩包的文件结构请见 [org.wvt.horizonmgr.service.horizonmgr2.mod.InstalledMod]
 *
 */
class ZipMod internal constructor(
    internal val file: File,
    private val zipFile: ZipFile = ZipFile(file)
) {
    class NotZipModFileException() : Exception("Not a zip mod file.")


    companion object {

        @Throws(NotZipModFileException::class)
        fun fromFile(file: File): ZipMod {
            val zipFile = try {
                ZipFile(file)
            } catch (e: ZipException) {
                throw NotZipModFileException()
            }
            if (isZipMod(zipFile)) {
                return ZipMod(file)
            } else {
                throw NotZipModFileException()
            }
        }

        private fun isZipMod(file: ZipFile): Boolean {
            return file.entries().asSequence().find {
                it.name.endsWith("mod.info")
            } != null
        }
    }

    class ModInfoNotFoundException() : Exception()

    fun getModInfo(): ModInfo {
        val entry = zipFile.entries().asSequence().find {
            it.name.endsWith("mod.info")
        } ?: throw ModInfoNotFoundException()

        val input = zipFile.getInputStream(entry)
        val modInfoStr = input.bufferedReader().use { it.readText() }

        return ModInfo.fromJson(modInfoStr)
    }

    fun getModIconStream(): InputStream? {
        val entry = zipFile.entries().asSequence().find {
            it.name.endsWith("mod_icon.png")
        } ?: return null

        return zipFile.getInputStream(entry)
    }
}