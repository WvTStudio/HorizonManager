package org.wvt.horizonmgr.service.horizonmgr2

import org.json.JSONObject
import java.io.File

/**
 * 该类代表一个已经安装的分包
 */
class LocalPackage constructor(
    private val pkgDir: File
) {
    // TODO: 2020/11/1 使该 Package 始终能返回最新数据
    private val manifestFile = pkgDir.resolve("manifest.json")
    private val installationInfoFile = pkgDir.resolve(".installation_info")
    private val graphicsFile = pkgDir.resolve(".cached_graphics")

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

    init {
        // Parse information
        parseInstallationInfo()
        parseManifest()
    }

    private fun parseManifest() {
        if (!manifestFile.exists() || !manifestFile.isFile) error("Missing manifest.json file")
        val jsonStr = manifestFile.readText()
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

    private fun parseInstallationInfo() {
        if (!installationInfoFile.exists() || !installationInfoFile.isFile) error("Missing .installation_info file")
        val jsonStr = installationInfoFile.readText()
        with(JSONObject(jsonStr)) {
            packageUUID = getString("uuid")
            installUUID = getString("internalId") // 该 UUID 用于唯一标识
            installTimestamp = getLong("timestamp")
            customName = optString("customName").takeIf { it.isNotEmpty() } // customName 是可选的
        }
    }



    /**
     * 获取分包的安装路径
     */
    fun getInstallDir(): File = pkgDir

    /**
     * 获取分包的名称
     */
    fun getName(): String = pack
    /**
     * 获取分包的描述
     */
    fun getDescription(): Map<String, String> = description

    fun getVersion() = packVersion
    fun getVersionCode() = packVersionCode
    fun getGame() = game
    fun getGameVersion() = gameVersion
    fun getDeveloper() = developer

    /**
     * 获取分包的 UUID
     */
    fun getPackageUUID(): String = packageUUID

    /**
     * 获取分包的自定义名称
     */
    fun getCustomName(): String? = customName

    /**
     * 获取安装时的时间戳
     */
    fun getInstallTimeStamp(): Long = installTimestamp

    /**
     * 获取安装时的 UUID
     */
    fun getInstallUUID(): String = installUUID

    /**
     * 获取图像压缩包
     */
    fun getCachedGraphics(): File = graphicsFile

    /**
     * 安装 Mod 到该 Package
     */
    fun installMod() {
        TODO("2020/10/29")
    }

    /**
     * 重命名
     */
    fun rename(newName: String) {
        val jsonStr = installationInfoFile.readText()
        val json = JSONObject(jsonStr)
        json.put("customName", newName)
        installationInfoFile.writeText(json.toString(4))
    }
}