package org.wvt.horizonmgr.service.respack

import org.wvt.horizonmgr.service.utils.calcSize
import java.io.File

class ResourcePackage internal constructor(val directory: File) {
    private val manifestFile = directory.resolve("manifest.json")

    class NotResPackDirException(
        val directory: File,
        val missingFiles: List<String>
    ) : Exception(
        "${directory.absolutePath} is not a resource pack directory. Missing ${missingFiles.size} files: ${
            missingFiles.joinToString(
                prefix = "[",
                postfix = "]"
            )
        }"
    )

    companion object {
        @Throws(NotResPackDirException::class)
        fun parseByDirectory(dir: File): ResourcePackage {
            if (isResPackDir(dir)) {
                return ResourcePackage(dir)
            } else {
                throw NotResPackDirException(dir, essentialFile.toMutableList() + "textures" + "pack_icon.png")
            }
        }

        val essentialFile = setOf("manifest.json")
        val optionalFile = setOf(
            "animation_controllers", "animations", "attachables", "entity",
            "fogs", "models", "particles", "render_controllers",
            "sounds", "texts", "texture_sets", "textures", "ui",
            "biomes_client.json", "blocks.json", "pack_icon.png",
            "sounds.json"
        )

        /**
         * 存在 [essentialFile]，且 [optionalFile] 存在至少一种则返回 true
         */
        fun isResPackDir(dir: File): Boolean {
            if (!dir.isDirectory) return false
            val files = dir.listFiles() ?: return false
            essentialFile.forEach {
                if (files.find { file -> file.name == it } == null) return false
            }
            var optionalFiles = 0
            optionalFile.forEach {
                if (files.find { file -> file.name == it } != null) optionalFiles++
            }
            return optionalFiles > 0
        }
    }

    /**
     * 当解析失败时抛出错误
     */
    fun getManifest(): ResourcePackManifest {
        val manifestStr = manifestFile.readText()
        return ResourcePackManifest.fromJson(manifestStr)
    }

    fun getIcon(): File? {
        return directory.resolve("pack_icon.png").takeIf { it.exists() }
    }

    suspend fun calcSize(): Long {
        return directory.calcSize().fileCount
    }
}