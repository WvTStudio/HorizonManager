package org.wvt.horizonmgr.service.hzpack

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.wvt.horizonmgr.service.CoroutineZip
import org.wvt.horizonmgr.service.level.MCLevelManager
import org.wvt.horizonmgr.service.mod.InstalledMod
import org.wvt.horizonmgr.service.mod.ZipMod
import org.wvt.horizonmgr.service.respack.ResourcePackManager
import org.wvt.horizonmgr.service.utils.translateToValidFile
import java.io.File
import java.util.*

/**
 * 该类代表一个已经安装的分包
 */
class InstalledPackage(val packageDirectory: File) {
    class PackageDoesNotExistsException() : Exception("Package does not exists.")
    class MissingManifestFile() : Exception("Could not find the manifest file.")
    class MissingInstallationInfoFile() : Exception("Could not find .installation_info file")

    // TODO: 2020/11/1 使该 Package 始终能返回最新数据
    private val manifestFile = packageDirectory.resolve("manifest.json")
    private val installationInfoFile = packageDirectory.resolve(".installation_info")
    private val graphicsFile = packageDirectory.resolve(".cached_graphics")
    private val modDir = packageDirectory.resolve("innercore").resolve("mods")

    fun getManifest(): PackageManifest {
        if (!manifestFile.exists() || !manifestFile.isFile) throw MissingManifestFile()
        return PackageManifestWrapper.fromJson(manifestFile.readText())
    }

    fun getInstallationInfo(): InstallationInfo {
        if (!installationInfoFile.exists() || !installationInfoFile.isFile) throw MissingInstallationInfoFile()
        return InstallationInfo.fromJson(installationInfoFile.readText())
    }

    /**
     * 重命名
     */
    suspend fun rename(newName: String) = withContext(Dispatchers.IO) {
        val jsonStr = installationInfoFile.readText()
        val json = JSONObject(jsonStr)
        json.put("customName", newName)
        installationInfoFile.writeText(json.toString(4))
    }

    /**
     * 获取图像压缩包
     */
    fun getCachedGraphics(): PackageGraphics? = try {
        PackageGraphics.parse(graphicsFile)
    } catch (e: Exception) {
        null
    }

    /**
     * 安装 Mod 到该 Package
     */
    suspend fun installMod(mod: ZipMod) {
        val modInfo = mod.getModInfo()
        val task = CoroutineZip.unzip(
            zipFile = mod.file,
            outDir = modDir.resolve(modInfo.name).translateToValidFile(),
            autoUnbox = true
        )
        task.await()
    }

    /**
     * 获取该分包的所有 Mod
     */
    suspend fun getMods(): List<InstalledMod> = withContext(Dispatchers.IO) {
        val result = mutableListOf<InstalledMod>()

        val modsDir = packageDirectory.resolve("innercore").resolve("mods")
        if (!modsDir.exists()) modsDir.mkdirs()

        for (dir in modsDir.listFiles()!!) {
            // Mod只会是文件夹
            if (dir.isFile) continue
            try {
                result.add(InstalledMod(dir))
            } catch (e: Exception) {
                e.printStackTrace()
                continue
            }
        }
        return@withContext result
    }

    suspend fun delete() {
        withContext(Dispatchers.IO) {
            packageDirectory.deleteRecursively()
        }
    }

    suspend fun clone(newName: String) = withContext(Dispatchers.IO) {
        val targetDir = packageDirectory.parentFile!!.resolve(newName).translateToValidFile()
        packageDirectory.copyRecursively(targetDir)
        InstalledPackage(targetDir)

        val oldInfo = InstallationInfo.fromJson(
            targetDir.resolve(".installation_info").readText()
        )

        val newInfo = oldInfo.copy(
            customName = newName,
            internalId = UUID.randomUUID().toString().toLowerCase(Locale.ROOT)
        )
        targetDir.resolve(".installation_info").writeText(newInfo.toJson())
        return@withContext newInfo
    }

    private val mLevelManager by lazy {
        MCLevelManager(packageDirectory.resolve("worlds"))
    }

    fun getLevelManager(): MCLevelManager {
        return mLevelManager
    }

    private val mResManager by lazy {
        val directory = packageDirectory.resolve("innercore").resolve("resource_packs")
        if (!directory.exists()) {
            directory.mkdirs()
            directory.mkdir()
        }
        ResourcePackManager(directory)
    }

    fun getResManager(): ResourcePackManager {
        return mResManager
    }
}