package org.wvt.horizonmgr.service.hzpack

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipError
import java.util.zip.ZipFile

class PackageGraphics internal constructor(
    val file: File,
    internal val zipFile: ZipFile = ZipFile(file)
) {
    interface Item {
        val name: String
        fun getInputStream(): InputStream
    }

    class NotPackageGraphicsException() : Exception()

    private inner class ItemImpl constructor(
        val zipEntry: ZipEntry
    ) : Item {
        override val name: String = zipEntry.name
        override fun getInputStream(): InputStream {
            return zipFile.getInputStream(zipEntry)
        }
    }

    companion object {
        @Throws(NotPackageGraphicsException::class, IOException::class)
        fun parse(file: File): PackageGraphics {
            val zipFile = try {
                ZipFile(file)
            } catch (e: ZipError) {
                throw NotPackageGraphicsException()
            }
            return PackageGraphics(file, zipFile)
        }
    }

    fun getBackgrounds(): List<Item> {
        val backgrounds = zipFile.entries().asSequence().filter { it.name.startsWith("background") }
        return backgrounds.map {
            ItemImpl(it)
        }.toList()
    }

    fun getThumbnails(): List<Item> {
        val thumbnails = zipFile.entries().asSequence().filter {
            it.name.startsWith("thumbnail")
        }
        return thumbnails.map {
            ItemImpl(it)
        }.toList()
    }
}