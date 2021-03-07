package org.wvt.horizonmgr.service.level

import org.wvt.horizonmgr.service.hzpack.InstalledPackage
import java.io.File
import java.io.IOException

interface TransportResult {
    val errors: List<TransportError>
}

data class TransportError(
    val file: File,
    val e: IOException
)

class LevelTransporter(
    private val mcLevelPath: String
) {
    class TransportResultImpl() : TransportResult {
        val _errors: MutableList<TransportError> = mutableListOf()
        override val errors: List<TransportError> = _errors
    }

    fun copyToHZ(mcLevel: MCLevel, hzPackage: InstalledPackage): TransportResult {
        return transportToHZ(mcLevel, hzPackage, false)
    }

    fun moveToHZ(mcLevel: MCLevel, hzPackage: InstalledPackage): TransportResult {
        return transportToHZ(mcLevel, hzPackage, true)
    }

    fun copyToMC(mcLevel: MCLevel): TransportResult {
        return transportToMC(mcLevel, false)
    }

    fun moveToMC(mcLevel: MCLevel): TransportResult {
        return transportToMC(mcLevel, true)
    }

    private fun transportToHZ(mcLevel: MCLevel, hzPackage: InstalledPackage, deleteSource: Boolean): TransportResult {
        val mcLevelDir = mcLevel.directory
        val levelDirName = mcLevelDir.name
        val hzPackageDir = hzPackage.getInstallDir()
        val hzLevelDir = hzPackageDir.resolve("worlds").resolve(levelDirName)
        val result = TransportResultImpl()

        mcLevelDir.copyRecursively(hzLevelDir, onError = { file, e ->
            result._errors.add(TransportError(file, e))
            OnErrorAction.SKIP
        })

        if (deleteSource) {
            mcLevelDir.deleteRecursively()
        }

        return result
    }

    private fun transportToMC(level: MCLevel, deleteSource: Boolean): TransportResult {
        val levelDir = level.directory
        val levelDirName = levelDir.name
        val targetMCLevelDir = File(mcLevelPath).resolve(levelDirName)
        val result = TransportResultImpl()
        levelDir.copyRecursively(targetMCLevelDir, onError = { file, e ->
            result._errors.add(TransportError(file, e))
            OnErrorAction.SKIP
        })
        if (deleteSource) {
            levelDir.deleteRecursively()
        }
        return result
    }
}