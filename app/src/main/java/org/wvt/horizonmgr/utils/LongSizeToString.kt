package org.wvt.horizonmgr.utils

fun longSizeToString(size: Long): String {
    return when {
        size < 1024L -> {
            "${size}B"
        }
        size < 1024L * 1024L -> {
            val tmp = size / 1024f
            "%.2fKB".format(tmp)
        }
        size < 1024L * 1024L * 1024L -> {
            val tmp = size / 1024L / 1024f
            "%.2fMB".format(tmp)
        }
        else -> {
            val tmp = size / 1024L / 1024L / 1024f
            "%.2fGB".format(tmp)
        }
    }
}