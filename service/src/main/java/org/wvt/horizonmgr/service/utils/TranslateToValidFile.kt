package org.wvt.horizonmgr.service.utils

import java.io.File

fun File.translateToValidFile(): File {
    val validName = name.replace(' ', '_').replace('/', '_')
    val parentDir = parentFile!!
    var i = 0
    var translatedFile = parentDir.resolve(validName)
    while (translatedFile.exists()) {
        translatedFile = parentDir.resolve("$validName(${++i})")
    }
    return translatedFile
}

fun translateToValidFileName(name: String): String {
    return name.replace(' ', '_').replace('/', '_')
}