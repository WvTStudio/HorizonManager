package org.wvt.horizonmgr.service

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.wvt.horizonmgr.utils.CoroutineZip
import org.wvt.horizonmgr.utils.toArray
import me.konyaco.horizonmgr.service.utils.translateToValidFile
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import java.util.zip.ZipFile

/**
 * 在线下载的 Mod 先解压、解析、生成解析文件
 * 手动导入的 Mod，先解压
 */
class HorizonManager private constructor(context: Context) {

    companion object {
        private var instance: WeakReference<HorizonManager?> = WeakReference(null)

        fun createInstance(context: Context): HorizonManager {
            val h = HorizonManager(context)
            instance = WeakReference(h)
            return h
        }

        fun getOrCreateInstance(context: Context): HorizonManager {
            return instance.get() ?: createInstance(context)
        }

        fun getInstance(): HorizonManager {
            return instance.get()!!
        }
    }

    private val downloadDir = context.filesDir.resolve("downloads")
        get() = field.also { if (!it.exists()) it.mkdirs() }

    private val downloadModsDir = downloadDir.resolve("mods")
        get() = field.also { if (!it.exists()) it.mkdirs() }

    private val downloadPacksDir = downloadDir.resolve("packs")
        get() = field.also { if (!it.exists()) it.mkdirs() }

    private val packageCache: MutableList<LocalPackage> = mutableListOf()
    private val horizonDir =
        Environment.getExternalStorageDirectory().resolve("games").resolve("horizon")
    private val packDir = horizonDir.resolve("packs")

    data class PackageNotFoundException(val uuid: String) : Exception("未找到 UUID 为: $uuid 的分包")

    sealed class FileType {
        object Mod : FileType()
        object Package : FileType()
        object Resource : FileType()
    }

    /**
     * 获取压缩包的类型
     * @return 未知类型返回 null
     */
    suspend fun getFileType(zipFile: File): FileType? = withContext(Dispatchers.IO) {
        val zip = ZipFile(zipFile)

        // single-directory-file
        var SDF = false
        val entries = zip.entries().toList()

        for (entry in entries) {
            if (entry.isDirectory) continue
            // FIXME 需要准确判断
            when {
                entry.name.endsWith("mod.info") -> {
                    return@withContext FileType.Mod
                }
                entry.name.endsWith("manifest.json") -> {
                    val str = zip.getInputStream(entry).bufferedReader().use { it.readText() }
                    try {
                        parsePackageManifest(str)
                        return@withContext FileType.Package
                    } catch (e: Exception) {
                    }
                    try {
                        parseResourceManifest(str)
                        return@withContext FileType.Resource
                    } catch (e: Exception) {
                    }
                }
            }
        }
        return@withContext null
    }

    data class LocalPackage(
        val customName: String,
        val path: String,
        val uuid: String, // 分包的 UUID
        val packageUUID: String, // 分包安装后生成的 UUID
        val manifest: String,
        val installTimeStamp: Long
    )

    data class InstallationInfo(
        val packageId: String,
        val timeStamp: Long,
        val customName: String,
        val internalId: String
    ) {
        companion object {
            fun fromJson(jsonStr: String): InstallationInfo? {
                return try {
                    with(JSONObject(jsonStr)) {
                        InstallationInfo(
                            packageId = getString("uuid"),
                            timeStamp = getLong("timestamp"),
                            customName = getString("customName"),
                            internalId = getString("internalId")
                        )
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }

        fun toJson(): String {
            return with(JSONObject()) {
                put("uuid", packageId)
                put("timestamp", timeStamp)
                put("customName", customName)
                put("internalId", internalId)
            }.toString(4)
        }
    }

    data class PackageManifest(
        val gameVersion: String,
        val packName: String,
        val packVersionName: String,
        val packVersionCode: Int,
        val developer: String,
        val description: String
    )

    suspend fun parsePackageManifest(jsonStr: String): PackageManifest {
        return with(JSONObject(jsonStr)) {
            PackageManifest(
                gameVersion = getString("game"),
                packName = getString("pack"),
                packVersionName = getString("packVersion"),
                packVersionCode = getInt("packVersionCode"),
                developer = getString("developer"),
                description = getJSONObject("description").getString("en")
            )
        }
    }

    /**
     * 获取指定分包的信息
     * @return null: 分包未找到
     */
    suspend fun getPackageInfo(packageUUID: String): LocalPackage? = withContext(Dispatchers.IO) {
        packageCache.find { it.uuid == packageUUID }?.let {
            return@withContext it
        }
        getLocalPackages().forEach {
            if (it.uuid == packageUUID) {
                return@withContext it
            }
        }
        return@withContext null
    }

    /**
     * 从压缩包中获取分包的信息
     * @return 分包信息，null: 该压缩包不是有效的分包
     * @throws [Exception] 文件读写错误、其他错误
     */
    suspend fun getPackageInfoFromZip(): LocalPackage? {
        TODO()
    }

    /**
     * 获取本地的所有分包
     */
    suspend fun getLocalPackages(): List<LocalPackage> = withContext(Dispatchers.IO) {
        val subDirs = packDir.listFiles() ?: emptyArray()

        val packages = mutableListOf<LocalPackage>()

        for (dir in subDirs) {
            if (dir.resolve("hz_mgr.xml").exists()) {
                // 是通过管理器安装的模组
            } else {
                val installInfoFile = dir.resolve(".installation_info")
                if (!installInfoFile.exists()) continue

                val jsonStr = installInfoFile.readText()
                val insInfo = InstallationInfo.fromJson(jsonStr) ?: continue

                val manifestStr = dir.resolve("manifest.json").readText()
                packages.add(
                    LocalPackage(
                        customName = insInfo.customName,
                        installTimeStamp = insInfo.timeStamp,
                        uuid = insInfo.internalId,
                        packageUUID = insInfo.packageId,
                        manifest = manifestStr,
                        path = dir.absolutePath
                    )
                )
            }
        }
        packageCache.clear()
        packageCache.addAll(packages)
        return@withContext packages
    }

    /**
     * 重命名指定分包
     */
    suspend fun renamePackage(pkgId: String, newName: String) = withContext(Dispatchers.IO) {
        val pkgInfo = getPackageInfo(pkgId) ?: throw PackageNotFoundException(pkgId)
        val pkgDir = File(pkgInfo.path)
        val installInfoFile = pkgDir.resolve(".installation_info")
        val newInfo =
            InstallationInfo.fromJson(installInfoFile.readText())?.copy(customName = newName)
                ?: error("解析JSON失败")
        installInfoFile.delete()
        installInfoFile.writeText(newInfo.toJson())
        pkgDir.renameTo(pkgDir.parentFile!!.resolve(newName).translateToValidFile())
    }

    /**
     * 删除一个分包
     */
    suspend fun deletePackage(pkgId: String) = withContext(Dispatchers.IO) {
        val pkgInfo = getPackageInfo(pkgId) ?: throw PackageNotFoundException(pkgId)
        File(pkgInfo.path).deleteRecursively()
    }

    /**
     * 克隆一个分包
     */
    suspend fun clonePackage(pkgId: String, newPackageName: String): InstallationInfo =
        withContext(Dispatchers.IO) {
            val pkgInfo = getPackageInfo(pkgId) ?: throw PackageNotFoundException(pkgId)
            val sourceDir = File(pkgInfo.path)
            val targetDir = sourceDir.parentFile!!.resolve(newPackageName).translateToValidFile()
            sourceDir.copyRecursively(targetDir)
            val oldInfo = InstallationInfo.fromJson(
                targetDir.resolve(".installation_info").readText()
            ) ?: error("解析JSON失败")
            val newInfo = oldInfo.copy(
                customName = newPackageName,
                internalId = UUID.randomUUID().toString().toLowerCase(Locale.ROOT)
            )
            targetDir.resolve(".installation_info").writeText(newInfo.toJson())
            return@withContext newInfo
        }

    /**
     * 解析一个下载的分包，存储到管理器目录
     * 解析后将生成额外文件，标志了该分包的源头、版本等信息
     */
    suspend fun parsePackage() {
        TODO()
    }

    /**
     * 安装一个分包到本地
     * 安装后，管理器将会生成一个额外文件，它将标志该分包的源头、版本等信息
     * @return UUID of the installed package
     */
    suspend fun installPackage(
        packageName: String,
        packageZip: File,
        graphicsZip: File,
        packageUUID: String?
    ): String =
        withContext(Dispatchers.IO) {
            if (packDir.exists().not()) packDir.mkdirs()
            // 根据指定分包名称生成目录 TODO 检查是否合法
            val outDir = packDir.resolve(packageName).translateToValidFile().also { it.mkdirs() }
            // 创建开始安装的文件标志
            outDir.resolve(".installation_started").createNewFile()
            // 直接解压到 packs 文件夹
            CoroutineZip.unzip(packageZip, outDir).await()
            // 预览图压缩包直接复制改名
            graphicsZip.copyTo(outDir.resolve(".cached_graphics"))
            val uuid = UUID.randomUUID().toString().toLowerCase(Locale.ROOT)
            outDir.resolve(".installation_info").outputStream().writer().use {
                val jsonStr = InstallationInfo(
                    packageId = packageUUID ?: UUID.randomUUID().toString()
                        .toLowerCase(Locale.ROOT),
                    internalId = uuid,
                    timeStamp = Date().time,
                    customName = packageName
                ).toJson()
                it.write(jsonStr)
            }
            outDir.resolve(".installation_complete").createNewFile()
            return@withContext uuid
        }

    data class InstalledModInfo(
        val source: String,
        val name: String,
        val description: String,
        val versionName: String,
        val author: String,
        val enable: Boolean,
        val iconPath: String?,
        val path: String
    )

    data class UninstalledModInfo(
        val source: String,
        val name: String,
        val description: String,
        val versionName: String,
        val author: String,
        val path: String
    )

    /**
     * 解析一个 Mod，将其存储到管理器的目录
     * 解析将会生成一个额外文件，包括 Mod 的来源、Id、版本号等
     */
    suspend fun parseMod(modZip: File) {
        TODO()
    }

    /**
     * 删除一个解析过的 Mod
     */
    suspend fun deleteParsedMod() {
        TODO()
    }

    /**
     * 获取存储在管理器内部的模组
     */
    suspend fun getParsedMods() {
        TODO()
    }

    /**
     * 获取管理器解析后的 Mod 的信息
     */
    suspend fun getParsedModInfo() {
        TODO()
    }

    /**
     * 获取指定分包的所有 Mod
     * @throws [PackageNotFoundException], [Exception]
     */
    suspend fun getMods(packageUUID: String): List<InstalledModInfo> = withContext(Dispatchers.IO) {
        val pkg = getPackageInfo(packageUUID) ?: throw PackageNotFoundException(packageUUID)
        val modsDir = File(pkg.path).resolve("innercore").resolve("mods")

        if (!modsDir.exists()) return@withContext emptyList()

        val result = mutableListOf<InstalledModInfo>()
        modsDir.listFiles().forEach { dir ->
            if (dir.isFile) return@forEach
            val info = try {
                val icon = dir.resolve("mod_icon.png").takeIf { it.exists() }
                val infoStr = dir.resolve("mod.info").readText()
                val configStr = dir.resolve("config.json").takeIf { it.exists() }?.readText()

                val isEnabled = configStr?.let { JSONObject(it).getBoolean("enabled") } ?: false

                with(JSONObject(infoStr)) {
                    InstalledModInfo(
                        source = "unknown",
                        name = getString("name"),
                        author = getString("author"),
                        description = getString("description"),
                        versionName = getString("version"),
                        enable = isEnabled,
                        iconPath = icon?.absolutePath,
                        path = dir.absolutePath
                    )
                }
            } catch (e: Throwable) {
                return@forEach
            }
            result.add(info)
        }
        return@withContext result
    }

    /**
     * 安装 MOD 到指定分包
     * TODO 进度
     */
    suspend fun installMod(targetPkgId: String, modZip: File): Unit {
        val pkgInfo = getPackageInfo(targetPkgId) ?: throw PackageNotFoundException(targetPkgId)
        val modDir = File(pkgInfo.path).resolve("innercore").resolve("mods")
        val task = CoroutineZip.unzip(modZip, modDir, createContainerDir = true, autoUnbox = true)
        task.await()
    }

    /**
     * 启用指定分包的模组
     */
    suspend fun enableMod(targetPkgId: String, modId: String): Unit {
        TODO()
    }

    /**
     * 禁用指定分包的指定模组
     */
    suspend fun disableMod(targetPkgId: String, modId: String): Unit {
        TODO()
    }

    /**
     * 删除指定模组
     */
    suspend fun deleteMod(targetPkgId: String, targetModId: String) {
        TODO()
    }

    /**
     * 根据路径启用模组
     */
    suspend fun enableModByPath(path: String) = withContext(Dispatchers.IO) {
        val configFile = File(path).resolve("config.json")
        val configStr = configFile.takeIf { it.exists() }?.readText()
        val config = configStr?.let { JSONObject(configStr) } ?: JSONObject()
        config.put("enabled", true)
        configFile.writeText(config.toString(4))
    }

    /**
     * 根据路径禁用模组
     */
    suspend fun disableModByPath(path: String) = withContext<Unit>(Dispatchers.IO) {
        val configFile = File(path).resolve("config.json")
        val configStr = configFile.takeIf { it.exists() }?.readText()
        val config = configStr?.let { JSONObject(it) } ?: JSONObject()
        config.put("enabled", false)
        configFile.writeText(config.toString(4))
    }

    /**
     * 根据路径删除模组
     */
    suspend fun deleteModByPath(path: String) = withContext<Unit>(Dispatchers.IO) {
        val configFile = File(path).resolve("config.json")
        if (configFile.exists()) {
            File(path).deleteRecursively()
        }
    }

    data class LevelInfo(
        val name: String,
        val path: String,
        val screenshot: String?
    )

    /**
     * 获取指定分包的所有地图
     */
    suspend fun getICLevels(pkgId: String): List<LevelInfo> = withContext(Dispatchers.IO) {
        val packInfo = getPackageInfo(pkgId) ?: throw PackageNotFoundException(pkgId)
        val worldsDir = File(packInfo.path).resolve("worlds")
        if (!worldsDir.exists()) return@withContext emptyList()
        val result = mutableListOf<LevelInfo>()
        worldsDir.listFiles()!!.forEachIndexed { index, file ->
            try {
                if (!file.isDirectory) return@forEachIndexed
                val imgPath = file.resolve("world_icon.jpeg").takeIf { it.exists() }?.absolutePath
                val levelname = file.resolve("levelname.txt").readText()
                result.add(
                    LevelInfo(
                        name = levelname,
                        path = file.absolutePath,
                        screenshot = imgPath
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return@withContext result
    }

    suspend fun getMCLevels(): List<LevelInfo> = withContext(Dispatchers.IO) {
        val worldsDir = Environment.getExternalStorageDirectory()
            .resolve("games")
            .resolve("com.mojang")
            .resolve("packs")
            .resolve("minecraftWorlds")
        if (!worldsDir.exists()) return@withContext emptyList()
        val result = mutableListOf<LevelInfo>()
        worldsDir.listFiles()!!.forEachIndexed { index, file ->
            try {
                if (!file.isDirectory) return@forEachIndexed
                val imgPath = file.resolve("world_icon.jpeg").takeIf { it.exists() }?.absolutePath
                val levelname = file.resolve("levelname.txt").readText()
                result.add(
                    LevelInfo(
                        name = levelname,
                        path = file.absolutePath,
                        screenshot = imgPath
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return@withContext result
    }

    /**
     * 安装存档到指定分包
     */
    suspend fun installLevel(targetPkgId: String, mapZip: File) {
        TODO()
    }

    suspend fun deleteLevel(targetPkgId: String, levelId: String) {
        TODO()
    }

    suspend fun renameLevel(targetPkgId: String, levelId: String, newName: String) {
        TODO()
    }

    /**
     * 删除指定路径的存档
     */
    suspend fun deleteLevelByPath(path: String) = withContext(Dispatchers.IO) {
        val dir = File(path)
        if (dir.resolve("levelname.txt").exists().not()) error("This is not a level")
        dir.deleteRecursively()
    }

    /**
     * 重命名指定路径的存档
     */
    suspend fun renameLevelNameByPath(path: String, newName: String) = withContext(Dispatchers.IO) {
        val dir = File(path)
        val levelNameFile = dir.resolve("levelname.txt")
        if (levelNameFile.exists().not()) error("This is not a level")
        levelNameFile.writeText(newName)
    }

    data class ResourceManifest(
        val formatVersion: Int,
        val header: Header,
        val modules: List<Module>
    ) {
        data class Header(
            val description: String,
            val name: String,
            val uuid: String,
            val version: IntArray,
            val minEngineVersion: IntArray
        )

        data class Module(
            val description: String,
            val type: String,
            val uuid: String,
            val version: IntArray
        )
    }

    data class ResourcePack(
        val path: String,
        val manifest: String
    )

    suspend fun parseResourceManifest(jsonStr: String): ResourceManifest {
        return with(JSONObject(jsonStr)) {
            ResourceManifest(
                formatVersion = getInt("format_version"),
                header = getJSONObject("header").let {
                    ResourceManifest.Header(
                        description = it.getString("description"),
                        name = it.getString("name"),
                        uuid = it.getString("uuid"),
                        version = it.getJSONArray("version").toArray<Int>().toIntArray(),
                        minEngineVersion = it.getJSONArray("min_engine_version").toArray<Int>()
                            .toIntArray()
                    )
                },
                modules = getJSONArray("modules").toArray<JSONObject>().map {
                    ResourceManifest.Module(
                        description = it.getString("description"),
                        type = it.getString("type"),
                        uuid = it.getString("uuid"),
                        version = it.getJSONArray("version").toArray<Int>().toIntArray()
                    )
                }
            )
        }
    }

    @Deprecated("Not implemented")
    suspend fun getResourcePackages(packageUUID: String): List<ResourcePack> {
        val pkg = getPackageInfo(packageUUID) ?: throw PackageNotFoundException(packageUUID)
        val resDir = File(pkg.path).resolve("resource_packs").resolve("mods")
        TODO()
    }

    suspend fun getDownloadedMods(): List<UninstalledModInfo> = withContext(Dispatchers.IO) {
        val result = mutableListOf<UninstalledModInfo>()
        downloadModsDir.listFiles()!!.forEach { file ->
            val zipFile = try {
                ZipFile(file)
            } catch (e: Exception) {
                return@forEach
            }
            val entry = zipFile.entries().asSequence().find {
                it.name.endsWith("mod.info")
            } ?: return@forEach
            val input = zipFile.getInputStream(entry)
            val modInfoStr = input.bufferedReader().use { it.readText() }
            result.add(
                with(JSONObject(modInfoStr)) {
                    UninstalledModInfo(
                        source = "online",
                        name = getString("name"),
                        author = getString("author"),
                        description = getString("description"),
                        versionName = getString("version"),
                        path = file.absolutePath
                    )
                })
        }
        return@withContext result
    }
}