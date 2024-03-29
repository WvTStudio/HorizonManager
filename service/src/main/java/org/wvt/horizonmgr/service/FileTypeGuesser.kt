package org.wvt.horizonmgr.service

import org.wvt.horizonmgr.service.hzpack.ZipPackage
import org.wvt.horizonmgr.service.level.ZipMCLevel
import org.wvt.horizonmgr.service.mod.ZipMod
import org.wvt.horizonmgr.service.respack.ZipResourcePackage
import java.io.File
import java.util.zip.ZipException
import java.util.zip.ZipFile

object FileTypeGuesser {
    enum class FileType {
        HZPackage,
        MCLevel,
        ZipMod,
        ResourcePack,
        BehaviorPack,
        UNKNOWN
    }

    /**
     * 推测文件的类型
     * - 所有已知类型都是 Zip 压缩包
     */
    fun guess(file: File): FileType {
        if (!isZip(file)) {
            return FileType.UNKNOWN
        }

        return when {
            isResourcePack(file) -> FileType.ResourcePack
            isBehaviorPack(file) -> FileType.BehaviorPack
            isMCLevel(file) -> FileType.MCLevel
            isZipMod(file) -> FileType.ZipMod
            isHZPackage(file) -> FileType.HZPackage
            else -> FileType.UNKNOWN
        }
    }

    fun isZip(file: File): Boolean {
        return try {
            ZipFile(file)
            true
        } catch (e: ZipException) {
            // 表明这不是 Zip 文件
            false
        }
    }

    fun isZipMod(file: File): Boolean {
        return try {
            ZipMod.fromFile(file)
            true
        } catch (e: Exception) {
            // 表明这不是压缩包文件
            false
        }
    }

    /**
     * 判断是否是 MC 存档
     */
    fun isMCLevel(file: File): Boolean {
        val zip = try {
            ZipFile(file)
        } catch (e: ZipException) {
            return false
        }
        return ZipMCLevel.isZipMCLevel(zip)
    }

    fun isHZPackage(file: File): Boolean {
        return ZipPackage(file).isZipPackage()
    }


    /**
     * 压缩包中有 manifest.json 和其他任何一个 []resourcePackEntries] 条目则视为 ResourcePack
     */
    fun isResourcePack(file: File): Boolean {
        val zip = try {
            ZipFile(file)
        } catch (e: ZipException) {
            return false
        }
        return ZipResourcePackage.isZipResourcePack(zip)
    }

    private val behaviorPackEntries = setOf(
        "entities", "items", "loot_tables", "recipes", "scripts", "spawn_rules", "trading",
        "pack_icon.png"
    )

    /**
     * 压缩包中有 manifest.json 和其他任何一个 [behaviorPackEntries] 条目则视为 ResourcePack
     */
    fun isBehaviorPack(file: File): Boolean {
        val zip = try {
            ZipFile(file)
        } catch (e: ZipException) {
            return false
        }
        val hasManifest = zip.getEntry("manifest.json") != null
        if (!hasManifest) return false
        for (entry in behaviorPackEntries) {
            if (zip.getEntry(entry) != null) {
                return true
            }
        }
        return false
    }
}