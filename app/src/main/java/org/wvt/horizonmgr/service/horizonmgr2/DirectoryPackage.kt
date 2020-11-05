package org.wvt.horizonmgr.service.horizonmgr2

import java.io.File

/**
 * 该类代表一个还未安装的、已经解压的分包
 */
class DirectoryPackage private constructor(
    private val pkgDir: File
) {
    private val manifestFile = pkgDir.resolve("manifest.json")
    private lateinit var game: String
    private lateinit var gameVersion: String
    private lateinit var pack: String
    private lateinit var packVersion: String
    private var packVersionCode = -1
    private lateinit var developer: String
    private lateinit var description: MutableMap<String, String>

    private var customName: String? = null
    private lateinit var packageUUID: String
    private var installTimestamp: Long = -1
    private lateinit var installUUID: String

    // TODO: 2020/10/30  
}