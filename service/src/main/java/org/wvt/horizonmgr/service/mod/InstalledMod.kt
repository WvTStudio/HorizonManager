package org.wvt.horizonmgr.service.mod

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.File

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
class InstalledMod internal constructor(val modDir: File) {
    val iconFile = modDir.resolve("mod_icon.png").takeIf { it.exists() }
    private val infoFile = modDir.resolve("mod.info")
    private val configFile = modDir.resolve("config.json")

    // TODO-Proposal 使用 FileObserver 监听文件更改，一旦更改则将 isParsed 设为 false

    fun getModInfo(): ModInfo {
        val infoStr = infoFile.readText()
        return ModInfo.fromJson(infoStr)
    }

    fun isEnabled(): Boolean {
        // Parse Config
        val configStr =
            configFile.takeIf { it.exists() }?.readText() ?: error("Could not find config.json.")
        return Json.parseToJsonElement(configStr)
            .jsonObject["enabled"]
            ?.jsonPrimitive?.boolean ?: false
    }

    /**
     * 启用该 MOD
     *
     * 该方法将 `config.json` 的 `enabled` 键修改为 `true`
     *
     * 如果 `config.json` 不存在，将会自动创建一个
     */
    fun enable() {
        changeEnabled(true)
    }

    /**
     * 禁用该 MOD
     *
     * 该方法将 `config.json` 的 `enabled` 键修改为 `false`。
     *
     * 如果 `config.json` 不存在，将会自动创建一个
     */
    fun disable() {
        changeEnabled(false)
    }

    private fun changeEnabled(enabled: Boolean) {
        val configStr = configFile.takeIf { it.exists() }?.readText() ?: "{}"
        val json = Json.parseToJsonElement(configStr).jsonObject
        val entries = json.entries.mapNotNull {
            return@mapNotNull if (it.key == "enabled") null
            else it.key to it.value
        }.toTypedArray()

        val jsonPrinter = Json { prettyPrint = true }
        val result = JsonObject(mapOf("enabled" to JsonPrimitive(enabled), *entries))
        val jsonString = jsonPrinter.encodeToString(result)
        configFile.writeText(jsonString)
    }

    /**
     * 移除该 MOD
     *
     * 该方法直接将整个 MOD 文件夹删除
     */
    fun delete() {
        modDir.deleteRecursively()
    }
}