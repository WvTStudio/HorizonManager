package org.wvt.horizonmgr.service.hzpack

import kotlinx.serialization.SerializationException
import org.wvt.horizonmgr.service.hzpack.OldPackageManifest.Companion.toPackageManifest

/**
 * 本类支持新旧版本的 PackageManifest，并自动转换成新 PackageManifest
 */
object PackageManifestWrapper {
    fun fromJson(jsonStr: String): PackageManifest {
        return try {
            PackageManifest.fromJson(jsonStr)
        } catch (e: SerializationException) {
            OldPackageManifest.fromJson(jsonStr).toPackageManifest()
        }
    }
}