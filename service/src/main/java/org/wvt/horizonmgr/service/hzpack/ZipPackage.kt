package org.wvt.horizonmgr.service.hzpack

import java.io.File
import java.util.zip.ZipException
import java.util.zip.ZipFile

/**
 * 该类代表一个还未安装的，压缩包形式的分包
 */
class ZipPackage(val zipFile: File) {
    open class NotZipPackageException : Exception()
    class MissingManifestException : NotZipPackageException()

    fun getManifest(): PackageManifest {
        val zip = try {
            ZipFile(zipFile)
        } catch (e: ZipException) {
            throw NotZipPackageException()
        }

        // TODO: 2020/10/30 当压缩包内还有一个根目录文件夹时
        val jsonEntry = zip.getEntry("manifest.json") ?: throw MissingManifestException()
        val jsonStr = zip.getInputStream(jsonEntry).reader().readText()

        return PackageManifestWrapper.fromJson(jsonStr)
    }

    fun isZipPackage(): Boolean {
        val zip = try {
            ZipFile(zipFile)
        } catch (e: ZipException) {
            return false
        }
        zip.getEntry("manifest.json") ?: return false
        // TODO: 2021/3/12 检查更多项目
        return true
    }
}