package org.wvt.horizonmgr.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 计算 File 中所有文件的数量和总大小 单位：Byte
 * @return 数量 to 大小
 */
@OptIn(ExperimentalStdlibApi::class)
suspend fun File.calcSize(): Pair<Long, Long> = withContext(Dispatchers.IO) {
    val size = DeepRecursiveFunction<File, Pair<Long, Long>> {
        var count = 0L
        var size = 0L
        it.listFiles()!!.forEach {
            if (it.isFile) {
                count++
                size += it.length()
            } else if (it.isDirectory) {
                val (c, s) = callRecursive(it)
                count += c
                size += s
            }
        }
        return@DeepRecursiveFunction count to size
    }(this@calcSize)
    return@withContext size
}