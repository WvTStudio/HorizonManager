package org.wvt.horizonmgr.service.level

import android.os.Environment

class MCLevelManager {
    private val worldsDir = Environment.getExternalStorageDirectory()
        .resolve("games")
        .resolve("com.mojang")
        .resolve("minecraftWorlds")

    class WorldsDirectoryNotExists(): Exception()

    fun getLevels(): List<MCLevel> {
        if (!worldsDir.exists()) throw WorldsDirectoryNotExists()
        val dirs = worldsDir.listFiles() ?: return emptyList()
        val result = mutableListOf<MCLevel>()

        for (file in dirs) {
            if (!file.isDirectory) continue

            val level = try {
                MCLevel.parseByDirectory(file)
            } catch (e: Exception) {
                e.printStackTrace()
                continue
            }

            result.add(level)
        }

        return result
    }
}