package org.wvt.horizonmgr.service

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.wvt.horizonmgr.service.pack.ZipPackage
import org.wvt.horizonmgr.service.pack.InstalledPackage
import org.wvt.horizonmgr.service.utils.translateToValidFile
import java.io.File
import java.util.*

class HorizonManager constructor(context: Context) {
    private val horizonDir =
        Environment.getExternalStorageDirectory().resolve("games").resolve("horizon")
    private val packDir = horizonDir.resolve("packs")

    /**
     * 从本机中获取已安装的 Package
     */
    suspend fun getInstalledPackages(): List<InstalledPackage> = withContext(Dispatchers.IO) {
        val subDirs = packDir.listFiles() ?: emptyArray()
        val packages = mutableListOf<InstalledPackage>()

        for (dir in subDirs) {
            val pkg = try {
                InstalledPackage(dir)
            } catch (e: IllegalStateException) {
                continue
            }
            packages.add(pkg)
        }

        return@withContext packages
    }

    /**
     * 将 ZipPackage 安装到本机
     */
    suspend fun installPackage(
        zipPackage: ZipPackage,
        graphicsZip: File
    ): InstalledPackage = withContext(Dispatchers.IO) {
        if (packDir.exists().not()) packDir.mkdirs()
        // 根据指定分包名称生成目录
        val outDir =
            packDir.resolve(zipPackage.getName()).translateToValidFile().also { it.mkdirs() }
        // 创建开始安装的文件标志
        outDir.resolve(".installation_started").createNewFile()
        // 直接解压到 packs 文件夹
        CoroutineZip.unzip(zipPackage.getPackageFile(), outDir).await()
        // 预览图压缩包直接复制改名
        graphicsZip.copyTo(outDir.resolve(".cached_graphics"))
        val uuid = UUID.randomUUID().toString().toLowerCase(Locale.ROOT)
        outDir.resolve(".installation_info").outputStream().writer().use {
            val jsonStr = InstallationInfo(
                packageId = UUID.randomUUID().toString().toLowerCase(Locale.ROOT),
                internalId = uuid,
                timeStamp = Date().time,
                customName = zipPackage.getName()
            ).toJson()
            it.write(jsonStr)
        }
        outDir.resolve(".installation_complete").createNewFile()
        return@withContext InstalledPackage(outDir)
    }

    data class InstallationInfo(
        val packageId: String,
        val timeStamp: Long,
        val customName: String,
        val internalId: String
    ) {
        fun toJson(): String {
            return with(JSONObject()) {
                put("uuid", packageId)
                put("timestamp", timeStamp)
                put("customName", customName)
                put("internalId", internalId)
            }.toString(4)
        }
    }
}