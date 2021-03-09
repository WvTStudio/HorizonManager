package org.wvt.horizonmgr.service.mod

import java.io.File
import java.util.zip.ZipFile

/**
 * 该类代表一个 Zip 形式的，还未安装的 MOD
 * 该压缩包的文件结构请见 [org.wvt.horizonmgr.service.horizonmgr2.mod.InstalledMod]
 *
 */
class ZipMod internal constructor(
    internal val file: File
) {
    private val zipFile = ZipFile(file)

    companion object {
        fun fromFile(file: File): ZipMod {
            return ZipMod(file)
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
}