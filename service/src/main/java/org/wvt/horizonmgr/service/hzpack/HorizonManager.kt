package org.wvt.horizonmgr.service.hzpack

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.service.CoroutineZip
import org.wvt.horizonmgr.service.utils.translateToValidFile
import java.io.File
import java.util.*

class HorizonManager constructor(val horizonDir: File) {
    private val packDir = horizonDir.resolve("packs")

    data class GetResult(
        val packages: List<InstalledPackage>,
        val errors: List<ErrorEntry>
    )

    data class ErrorEntry(
        val file: File,
        val error: Throwable
    )

    /**
     * 从本机中获取已安装的 Package
     * 该方法把从 [horizonDir] 的所有子文件通过 [InstalledPackage.parseByDirectory] 方法转换成 [InstalledPackage]
     * 转换时遇到的所有错误都会放在 errors
     */
    suspend fun getInstalledPackages(): GetResult = withContext(Dispatchers.IO) {
        val subDirs = packDir.listFiles() ?: emptyArray()
        val packages = mutableListOf<InstalledPackage>()
        val exceptions = mutableListOf<ErrorEntry>()

        for (dir in subDirs) {
            val pkg = try {
                InstalledPackage.parseByDirectory(dir)
            } catch (e: Exception) {
                exceptions.add(ErrorEntry(dir, e))
                continue
            }
            packages.add(pkg)
        }
        return@withContext GetResult(packages, exceptions)
    }

    /**
     * 从本机中获取指定的分包
     * 由于要使用 [InstalledPackage::getInstallationInfo()] 方法，该方法会对分包进行格式验证
     * 如果格式不正确则返回 null
     */
    suspend fun getInstalledPackage(installUUID: String): InstalledPackage? =
        withContext(Dispatchers.IO) {
            getInstalledPackages().packages.find {
                try {
                    return@find it.getInstallationInfo().internalId == installUUID
                } catch (e: Exception) {
                    return@find false
                }
            }
        }

    /**
     * 将 ZipPackage 安装到本机
     * 安装步骤：
     * 在 packDir 中创建文件夹
     *  创建 .installation_started
     *  将分包压缩包解压到文件夹
     *  创建 .installation_info
     *  将 graphics zip 改名为 .cached_graphics 并复制到文件夹
     *  创建 .installation_complete
     */
    suspend fun installPackage(
        zipPackage: ZipPackage,
        graphicsZip: File?,
        packageUUID: String?,
        customName: String?
    ): InstalledPackage = withContext(Dispatchers.IO) {
        val manifest = zipPackage.getManifest()
        val customName = customName ?: manifest.pack
        if (packDir.exists().not()) packDir.mkdirs()
        // 根据指定分包名称生成目录
        val outDir =
            packDir.resolve(customName).translateToValidFile().also { it.mkdirs() }
        // 创建开始安装的文件标志
        outDir.resolve(".installation_started").createNewFile()
        // 直接解压到 packs 文件夹
        CoroutineZip.unzip(zipPackage.zipFile, outDir).await()
        // TODO: 2021/2/20 测试如果没有 graphics 是否能运行
        // 预览图压缩包直接复制改名
        graphicsZip?.copyTo(outDir.resolve(".cached_graphics"))
        val packageUUID = packageUUID ?: UUID.randomUUID().toString().toLowerCase(Locale.ENGLISH)
        outDir.resolve(".installation_info").outputStream().writer().use {
            val jsonStr = InstallationInfo(
                packageId = packageUUID,
                internalId = UUID.randomUUID().toString().toLowerCase(Locale.ENGLISH),
                timeStamp = Date().time,
                customName = customName
            ).toJson()
            it.write(jsonStr)
        }
        outDir.resolve(".installation_complete").createNewFile()
        return@withContext InstalledPackage.parseByDirectory(outDir)
    }
}