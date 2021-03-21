package org.wvt.horizonmgr.service.respack

import java.io.File
import java.io.InputStream
import java.util.zip.ZipException
import java.util.zip.ZipFile

class ZipResourcePackage internal constructor(
    val file: File,
    internal val zipFile: ZipFile = ZipFile(file)
) {
    class NotZipResPackException() : Exception()

    companion object {
        @Throws(NotZipResPackException::class)
        fun parse(file: File): ZipResourcePackage {
            val zip = try {
                ZipFile(file)
            } catch (e: ZipException) {
                throw NotZipResPackException()
            }
            if(!isZipResourcePack(zip)) throw NotZipResPackException()
            return ZipResourcePackage(file, zip)
        }

        /**
         * Resource Pack 根目录的所有条目（manifest.json 除外）
         */
        private val resourcePackEntries = setOf<String>(
            "animation_controllers", "animations", "attachables", "entity",
            "fogs", "models", "particles", "render_controllers",
            "sounds", "texts", "texture_sets", "textures", "ui",
            "biomes_client.json", "blocks.json", "pack_icon.png",
            "sounds.json"
        )

        fun isZipResourcePack(zipFile: ZipFile): Boolean {
            val hasManifest = zipFile.getEntry("manifest.json") != null
            if (!hasManifest) return false
            for (entry in resourcePackEntries) {
                if (zipFile.getEntry(entry) != null) {
                    return true
                }
            }
            return false
        }
    }

    /**
     * 当解析失败时抛出错误
     */
    fun getManifest(): ResourcePackManifest {
        val manifestStr = zipFile.getInputStream(zipFile.getEntry("manifest.json")).reader().readText()
        return ResourcePackManifest.fromJson(manifestStr)
    }

    fun getIcon(): InputStream? {
        val iconEntry = zipFile.getEntry("pack_icon.png")
        return iconEntry?.let { zipFile.getInputStream(it) }
    }
}