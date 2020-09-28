package org.wvt.horizonmgr.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.util.zip.ZipFile

object CoroutineZip {
    /**
     * @param createContainerDir 创建一个名称为压缩包名的文件夹
     * @param autoUnbox 自动拆箱，如果压缩包内有且只有一个文件夹（Single-Directory-File），则自动深入该文件夹
     */
    fun unzip(
        zipFile: File,
        outDir: File,
        createContainerDir: Boolean = false,
        autoUnbox: Boolean = false
    ): ProgressDeferred<Float, Unit> {
        val scope = CoroutineScope(Dispatchers.IO)
        return scope.progressAsync<Float, Unit> {
            val zip = ZipFile(zipFile)
            // 如果需要创建 Container，则在 Outdir 的基础上再创建一个压缩包同名文件夹
            val outDir =
                if (createContainerDir)
                    outDir.resolve(zipFile.name.substringBeforeLast('.'))
                else outDir
            outDir.mkdirs()
            val entries = zip.entries().toList()

            // 是否需要拆箱，如果 autoUnbox 未选择 则为 false，如果选择则进入检测流程
            // Single-Directory-File 需要被拆箱
            var SDF: Boolean = autoUnbox
            if (autoUnbox) {
                var rootDirectories = 0
                for (entry in entries) {
                    if (!entry.isDirectory && entry.name.count { it == File.separatorChar } == 0) {
                        // 该 entry 是一个根目录的文件，则直接不符合拆箱条件，跳出循环
                        SDF = false
                        break
                    } else if (entry.isDirectory && entry.name.count { it == File.separatorChar } == 1) {
                        // 该 entry 是根目录的一个文件夹
                        if (rootDirectories == 0) {
                            // 如果之前没有根目录文件夹，则记录
                            rootDirectories++
                        } else {
                            // 如果发现了第二个根目录文件夹，则不符合拆箱条件，跳出循环
                            break
                        }
                    }
                }
            }

            val totalCount = entries.size.toFloat()
            entries.forEachIndexed { index, entry ->
                val theFile =
                    if (SDF) outDir.resolve(entry.name.substringAfter(File.separatorChar)) // 跳过第一个文件夹
                    else outDir.resolve(entry.name)
                if (entry.isDirectory) {
                    theFile.mkdirs()
                } else {
                    if (theFile.parentFile?.exists() == false) {
                        theFile.parentFile?.mkdirs()
                    }
                    theFile.outputStream().use {
                        zip.getInputStream(entry).copyTo(it)
                    }
                }
                it.send(index / totalCount)
            }
            zip.close()
            it.close()
        }
    }
}