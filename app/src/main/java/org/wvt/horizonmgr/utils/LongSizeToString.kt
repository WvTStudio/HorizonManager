package org.wvt.horizonmgr.utils

fun longSizeToString(size: Long): String {
    return when {
        size >= 1024 * 1024 -> "${(size / 1024 / 1024)} MB"
        size >= 1024 -> "${(size / 1024)} KB"
        size >= 0 -> "$size B"
        else -> error("计算出错，size: $size")
    }
}