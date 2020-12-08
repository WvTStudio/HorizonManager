package me.konyaco.horizonmgr.service.mod

import org.json.JSONObject
import java.io.File
import java.io.InputStream

/**
 * 该类代表一个已经由 Horizon 安装的 Mod。已安装的 Mod 在设备上以文件夹的形式存在。该文件夹的结构是这样的：
 *
 * ```text
 * -- Root Directory
 *    |-d dev
 *    |-  ...
 *    |-f mod.info
 *    |-f mod_icon.png
 *    |-f config.json
 * ```
 *
 * `mod.info` 是一个 Json 格式的文件，存储着 Mod 的基本信息：
 *
 * - `author` -- mod 的作者名
 * - `name` -- mod 的名字
 * - `description` -- mod 的描述
 * - `version` -- mod 的版本名字符串（非整数型版本号）
 *
 * `mod_icon.png` 是该 Mod 的图标，**图标是可选的**
 *
 * `config.json` JSON 格式，该文件存放 MOD 的所有设置，用户可以修改。
 * 该 Json 中必定有 `enabled` 键，Horizon 将通过该键来判断 Mod 是否启用。
 */
class InstalledMod internal constructor(
    private val modDir: File
) {
    private val iconFile = modDir.resolve("mod_icon.png")
    private val infoFile = modDir.resolve("mod.info")
    private val configFile = modDir.resolve("config.json")

    private var name: String = ""
    private var author: String = ""
    private var description: String = ""
    private var versionName: String = ""
    private var isEnabled: Boolean = false

    // TODO-Proposal 使用 FileObserver 监听文件更改，一旦更改则将 isParsed 设为 false

    private var isParsed = false

    /**
     * 该方法解析 `mod.info` 的信息和 `config.json` 中的 `enabled` 键
     */
    private suspend fun parse() {
        // 只有在没有解析时才会重新解析
        if (!isParsed) {
            // Parse Info
            val infoStr = infoFile.readText()
            with(JSONObject(infoStr)) {
                name = getString("name")
                author = getString("author")
                description = getString("description")
                versionName = getString("version")
            }

            // Parse Config
            val configStr = configFile.takeIf { it.exists() }?.readText()
            isEnabled = configStr?.let { JSONObject(it).getBoolean("enabled") } ?: false

            isParsed = true
        }
    }

    suspend fun isEnabled(): Boolean {
        parse()
        return isEnabled
    }

    suspend fun getGraphics(): InputStream? {
        parse()
        return iconFile.takeIf { it.exists() }?.inputStream()
    }

    suspend fun getAuthor(): String {
        parse()
        return author
    }

    suspend fun getDescription(): String {
        parse()
        return description
    }

    /**
     * 启用该 MOD
     *
     * 该方法将 `config.json` 的 `enabled` 键修改为 `true`
     *
     * 如果 `config.json` 不存在，将会自动创建一个
     */
    suspend fun enable() {
        changeEnabled(true)
    }

    /**
     * 禁用该 MOD
     *
     * 该方法将 `config.json` 的 `enabled` 键修改为 `false`。
     *
     * 如果 `config.json` 不存在，将会自动创建一个
     */
    suspend fun disable() {
        changeEnabled(false)
    }

    private suspend fun changeEnabled(enabled: Boolean) {
        val configStr = configFile.takeIf { it.exists() }?.readText()
        val json = configStr?.let {
            JSONObject(configStr)
        } ?: JSONObject()
        json.put("enabled", enabled)
        configFile.writeText(json.toString(4))
    }

    /**
     * 移除该 MOD
     *
     * 该方法直接将整个 MOD 文件夹删除
     */
    suspend fun delete() {
        modDir.deleteRecursively()
    }
}