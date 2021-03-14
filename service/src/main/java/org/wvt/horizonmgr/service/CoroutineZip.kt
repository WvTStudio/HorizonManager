package org.wvt.horizonmgr.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.util.zip.ZipFile

object CoroutineZip {
    /**
     * 自动拆箱规则：
     * 如果压缩包内有且只有一个文件夹，则将该文件夹作为根目录解压
     *
     *
     * 实现原理：
     * * Zip 压缩包的条目全按照广度优先排序，因此根目录条目都排在前面
     *
     * 遍历压缩包内的所有条目
     *    如果是根目录的条目
     *        如果根目录的条目中出现任何文件
     *            则代表该压缩包并非有且只有一个文件夹，不符合拆箱要求
     *            直接跳出循环
     *        如果根目录的条目中出现第二个文件夹
     *            则不符合拆箱要求
     *            直接跳出循环
     *    否则 直接跳出循环
     * 遍历完成后，如果没有不符合拆箱要求的情况，则拆箱
     *
     * 拆箱原理：
     * 由于有且只有一个文件夹，压缩包内的所有条目均以 "<directory>/" 开头，截取第一个 "/" 之后的内容即可
     */
    fun unzip(
        zipFile: File,
        outDir: File,
        autoUnbox: Boolean = false
    ): ProgressDeferred<Float, Unit> {
        val scope = CoroutineScope(Dispatchers.IO)
        return scope.progressAsync<Float, Unit> {
            val zip = ZipFile(zipFile)
            outDir.mkdirs()
            outDir.mkdir()
            val entries = zip.entries().toList()

            // 是否需要拆箱，如果 autoUnbox 未选择 则为 false，如果选择则进入检测流程
            var unbox = false

            if (autoUnbox) {
                var rootDirectories = 0

                for (entry in entries) {
                    val separators = entry.name.count { it == '/' }
                    if (entry.isDirectory && separators == 1 && rootDirectories == 0) {
                        // 该 entry 是根目录的一个文件夹，且之前没坏吧发现过根目录文件夹
                        unbox = true
                        // 如果之前没有根目录文件夹，则记录
                        rootDirectories++
                        continue
                    } else if (!entry.isDirectory && separators == 0) {
                        // 该 entry 是一个根目录的文件，则直接不符合拆箱条件，跳出循环
                        break
                    } else {
                        break
                    }
                }
            }

            val totalCount = entries.size.toFloat()

            entries.forEachIndexed { index, entry ->
                val entryName =
                    if (autoUnbox && unbox) entry.name.substringAfter("/")
                    else entry.name
                val theFile = outDir.resolve(entryName)
                if (entry.isDirectory) {
                    theFile.mkdirs()
                    theFile.mkdir()
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