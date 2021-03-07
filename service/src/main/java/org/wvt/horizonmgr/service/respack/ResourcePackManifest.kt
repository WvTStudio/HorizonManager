package org.wvt.horizonmgr.service.respack

import kotlinx.serialization.*
import kotlinx.serialization.json.Json

@Serializable
data class ResourcePackManifest(
    @SerialName("format_version")
    val formatVersion: Int,
    val header: Header,
    val modules: List<Module>
) {
    @Serializable
    data class Header(
        val description: String,
        val name: String,
        val uuid: String,
        val version: List<Int>,
        @SerialName("min_engine_version")
        val minEngineVersion: List<Int>? = null
    )

    @Serializable
    data class Module(
        val description: String,
        val type: String,
        val uuid: String,
        val version: List<Int>
    )

    companion object {
        private val json = Json {
            prettyPrint = true
        }
        fun fromJson(str: String): ResourcePackManifest {
            return json.decodeFromString(str)
        }

        fun ResourcePackManifest.toJson(): String {
            return json.encodeToString(this)
        }
    }
}