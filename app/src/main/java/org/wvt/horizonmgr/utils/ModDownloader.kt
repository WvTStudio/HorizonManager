package org.wvt.horizonmgr.utils

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.collect
import org.wvt.horizonmgr.service.ProgressDeferred
import org.wvt.horizonmgr.service.mod.ZipMod
import java.io.File

class ModDownloader(context: Context) {
    private val downloadDir = context.filesDir.resolve("downloads")
        get() = field.also { if (!it.exists()) it.mkdirs() }
    private val downloadModsDir = downloadDir.resolve("mods")
        get() = field.also { if (!it.exists()) it.mkdirs() }

    data class DownloadedMod(
        val path: String,
        val zipMod: ZipMod
    )

    suspend fun getDownloadedMods(): List<DownloadedMod> = withContext(Dispatchers.IO) {
        val result = mutableListOf<DownloadedMod>()
        downloadModsDir.listFiles()!!.forEach { file ->
            try {
                result.add(DownloadedMod(file.absolutePath, ZipMod.fromFile(file)))
            } catch (e: Exception) {
                // TODO: 2021/2/23 使该函数可以返回失败列表
                return@forEach
            }
        }
        return@withContext result
    }

    suspend fun downloadMod(name: String, url: String): ProgressDeferred<Float, File> =
        object : ProgressDeferred<Float, File> {
            private val scope = CoroutineScope(Dispatchers.IO)
            private val channel = Channel<Float>(Channel.UNLIMITED)
            private val job = scope.async<File> {
                val file = downloadModsDir.resolve("$name.zip")
                file.outputStream().use { output ->
                    val task = FileDownloader.newTask(url)
                    val total = task.connect()
                    task.setOutput(output)
                    val job = launch {
                        task.progress.collect {
                            channel.send((it.toDouble() / total.toDouble()).toFloat())
                        }
                    }
                    task.download()
                    job.cancel()
                }
                return@async file
            }

            override suspend fun await(): File = job.await()
            override suspend fun progressChannel(): ReceiveChannel<Float> = channel
        }
}