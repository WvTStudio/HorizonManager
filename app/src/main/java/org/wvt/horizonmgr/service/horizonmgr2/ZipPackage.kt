package org.wvt.horizonmgr.service.horizonmgr2

import org.json.JSONObject
import java.io.File
import java.util.zip.ZipFile

/**
 * 该类代表一个还未安装的，压缩包形式的分包
 */
class ZipPackage private constructor(
    private val zipFile: File
) {
    private lateinit var game: String
    private lateinit var gameVersion: String
    private lateinit var pack: String
    private lateinit var packVersion: String
    private var packVersionCode = -1
    private lateinit var developer: String
    private lateinit var description: MutableMap<String, String>

    init {
        parseManifest()
    }

    private fun parseManifest() {
        val zip = ZipFile(zipFile)

        // TODO: 2020/10/30 当压缩包内还有一个根目录文件夹时
        val jsonEntry = zip.getEntry("manifest.json") ?: error("Missing manifest.json")
        val jsonStr = zip.getInputStream(jsonEntry).reader().readText()

        with(JSONObject(jsonStr)) {
            game = getString("game")
            gameVersion = getString("gameVersion")
            pack = getString("pack")
            packVersion = getString("packVersion")
            packVersionCode = getInt("packVersionCode")
            developer = getString("developer")
            val desJson = getJSONObject("description")
            description = mutableMapOf()
            desJson.keys().forEach {
                description[it] = desJson.getString(it)
            }
        }
    }

    fun getPackageFile() = zipFile
    fun getName() = pack
    fun getDescription() = description
    fun getVersion() = packVersion
    fun getVersionCode() = packVersionCode
    fun getGame() = game
    fun getGameVersion() = gameVersion
    fun getDeveloper() = developer
}