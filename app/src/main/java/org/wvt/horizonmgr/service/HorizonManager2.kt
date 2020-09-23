package org.wvt.horizonmgr.service

import android.content.Context
import android.os.Environment
import java.io.File
import java.util.zip.ZipFile

@Deprecated("TODO")
interface HorizonManager2 {
    sealed class FileType {
        object OriginalPackage : FileType()
        object ManagerPackage : FileType()
        object OriginalMod : FileType()
        object ManagerMod : FileType()
        object Level : FileType()
    }

    suspend fun getZipFileType(zipFile: ZipFile): FileType

    enum class PackageSource { LOCAL, ONLINE }

    data class ParsedPackage(
        val source: PackageSource,
        val packageName: String,
        val developer: String,
        val versionName: String,
        val versionCode: Int,
    )

    data class InstalledPackage(
        val source: PackageSource,
        val packageId: String,
        val versionName: String,
        val versionCode: Int,

        val installId: String,
        val installTime: String,
        val name: String,
        val description: String,
        val developer: String,
    )

    suspend fun parsePackage(packageZip: ZipFile): ParsedPackage
    suspend fun getParsedPackages(): List<ParsedPackage>

    suspend fun getPackages(): List<InstalledPackage>
    suspend fun installPackage(packageId: String)
    suspend fun deletePackage(packageId: String)
    suspend fun renamePackage(packageId: String, newName: String)
    suspend fun clonePackage(packageId: String, newName: String)

    data class ParsedMod(
        val source: String,
        val id: String
    )

    data class InstalledMod(
        val source: PackageSource,
        val id: String,
        val installId: String,
        val installTime: String
    )

    /**
     * 获取管理器中解析过的所有模组
     */
    suspend fun getParsedMods(): List<ParsedMod>

    /**
     * 解析一个 Mod，将其存储到管理器的目录
     * 解析将会生成一个额外文件，包括 Mod 的来源、Id、版本号等
     */
    suspend fun parseMod(modZipFile: ZipFile): ParsedMod

    /**
     * 删除管理器内解析过的 Mod
     */
    suspend fun deleteParseMod(modId: String)

    /**
     * 获取一个解析过的 Mod
     */
    suspend fun getParsedModInfo(modId: String): ParsedMod

    /**
     * 获取指定分包中安装的所有 Mod，该分包必须是 [InstalledMod]
     */
    suspend fun getModsFromPackage(packageId: String): List<InstalledMod>

    /**
     * 安装指定 mod 到指定的分包
     */
    suspend fun installMod(packageId: String, modId: String)

    /**
     * 从指定分包中删除指定 Mod，该操作不会删除管理器内存储的 Mod
     */
    suspend fun deleteMod(packageId: String, modId: String)

    /**
     * 启用指定分包的指定 Mod
     */
    suspend fun enableMod(packageId: String, modId: String)

    /**
     * 禁用指定分包的指定 Mod
     */
    suspend fun disableMod(packageId: String, modId: String)


    suspend fun getLevels(packageId: String)
    suspend fun deleteLevel(packageId: String, levelId: String)
    suspend fun renameLevel(packageId: String, levelId: String, newName: String)

    companion object {
        private var instance: HorizonManager2? = null

        fun getInstance(): HorizonManager2 {
            return instance!!
        }

        fun init(context: Context) {
            instance = HorizonManager2Impl(context)
        }
    }
}

@Deprecated("Not implemented")
class HorizonManager2Impl : HorizonManager2 {
    private val filesDir: File
    private val horizonDir: File

    constructor(context: Context) {
        filesDir = context.filesDir
        horizonDir = Environment.getExternalStorageDirectory().resolve("games").resolve("horizon")
    }

    override suspend fun getZipFileType(zipFile: ZipFile): HorizonManager2.FileType {
        TODO("Not yet implemented")
    }

    override suspend fun parsePackage(packageZip: ZipFile): HorizonManager2.ParsedPackage {
        TODO("Not yet implemented")
    }

    override suspend fun getParsedPackages(): List<HorizonManager2.ParsedPackage> {
        TODO("Not yet implemented")
    }

    override suspend fun getPackages(): List<HorizonManager2.InstalledPackage> {
        TODO("Not yet implemented")
    }

    override suspend fun installPackage(packageId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deletePackage(packageId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun renamePackage(packageId: String, newName: String) {
        TODO("Not yet implemented")
    }

    override suspend fun clonePackage(packageId: String, newName: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getParsedMods(): List<HorizonManager2.ParsedMod> {
        TODO("Not yet implemented")
    }

    override suspend fun parseMod(modZipFile: ZipFile): HorizonManager2.ParsedMod {
        TODO("Not yet implemented")
    }

    override suspend fun deleteParseMod(modId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getParsedModInfo(modId: String): HorizonManager2.ParsedMod {
        TODO("Not yet implemented")
    }

    override suspend fun getModsFromPackage(packageId: String): List<HorizonManager2.InstalledMod> {
        TODO("Not yet implemented")
    }

    override suspend fun installMod(packageId: String, modId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMod(packageId: String, modId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun enableMod(packageId: String, modId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun disableMod(packageId: String, modId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getLevels(packageId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLevel(packageId: String, levelId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun renameLevel(packageId: String, levelId: String, newName: String) {
        TODO("Not yet implemented")
    }
}
