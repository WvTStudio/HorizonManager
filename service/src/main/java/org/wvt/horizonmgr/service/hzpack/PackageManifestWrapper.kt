package org.wvt.horizonmgr.service.hzpack

import kotlinx.serialization.SerializationException
import org.wvt.horizonmgr.service.hzpack.OldPackageManifest.Companion.toPackageManifest

/**
 * 本类支持新旧版本的 PackageManifest，并自动转换成新 PackageManifest
 */
object PackageManifestWrapper {
    /**
     * 只会抛出新 PackageManifest 导致的异常
     */
    @Throws(SerializationException::class)
    fun fromJson(jsonStr: String): PackageManifest {
        return try {
            PackageManifest.fromJson(jsonStr)
        } catch (e: SerializationException) {
            try {
                OldPackageManifest.fromJson(jsonStr).toPackageManifest()
            } catch (oldError: SerializationException) {
                throw e
            }
        }
    }
}