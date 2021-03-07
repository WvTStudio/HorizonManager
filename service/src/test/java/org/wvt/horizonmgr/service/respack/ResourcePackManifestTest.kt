package org.wvt.horizonmgr.service.respack

import org.junit.Test
import org.wvt.horizonmgr.service.respack.ResourcePackManifest.Companion.toJson

class ResourcePackManifestTest {
    @Test
    fun decode() {
        val json = """
            {
                "format_version": 1,
                "header": {
                    "description": "by zcl,交流群：787063231",
                    "name": "Zenith 128x",
                    "uuid": "66dd35b6-4f1b-4566-9c6b-43bdc9bd1219",
                    "version": [6, 6, 6]
                },
                "modules": [
                    {
                        "description": "by zcl,交流群：787063231",
                        "type": "resources",
                        "uuid": "6c04d1d5-1203-4192-ab4f-d932462f7c98",
                        "version": [7, 7, 6]
                    }
                ]
            }
        """.trimIndent()
        println(ResourcePackManifest.fromJson(json).toJson())
    }
}