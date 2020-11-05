package org.wvt.horizonmgr.service.horizonmgr2

import android.content.Context
import android.os.Environment
import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.utils.CoroutineZip
import org.wvt.horizonmgr.utils.translateToValidFile
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

class HorizonManager2 private constructor(context: Context) {
    companion object {
        private var instance: WeakReference<HorizonManager2?> = WeakReference(null)

        fun createInstance(context: Context): HorizonManager2 {
            val h = HorizonManager2(context)
            instance = WeakReference(h)
            return h
        }

        fun getOrCreateInstance(context: Context): HorizonManager2 {
            return instance.get() ?: createInstance(context)
        }
    }

    private val horizonDir =
        Environment.getExternalStorageDirectory().resolve("games").resolve("horizon")
    private val packDir = horizonDir.resolve("packs")

    fun getPackages(): List<LocalPackage> {
        val subDirs = packDir.listFiles() ?: emptyArray()
        val packages = mutableListOf<LocalPackage>()

        for (dir in subDirs) {
            val pkg = try {
                LocalPackage(dir)
            } catch (e: IllegalStateException) {
                continue
            }
            packages.add(pkg)
        }

        return packages
    }

    suspend fun installPackage(zipPackage: ZipPackage, graphicsZip: File): LocalPackage {
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
            val jsonStr = HorizonManager.InstallationInfo(
                packageId = UUID.randomUUID().toString().toLowerCase(Locale.ROOT),
                internalId = uuid,
                timeStamp = Date().time,
                customName = zipPackage.getName()
            ).toJson()
            it.write(jsonStr)
        }
        outDir.resolve(".installation_complete").createNewFile()
        return LocalPackage(outDir)
    }
}