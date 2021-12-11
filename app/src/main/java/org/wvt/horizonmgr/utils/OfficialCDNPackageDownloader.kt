package org.wvt.horizonmgr.utils

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import org.wvt.horizonmgr.webapi.pack.OfficialCDNPackage
import java.io.File
import java.util.concurrent.atomic.AtomicLong

private const val TAG = "OfficialCDNPackageDownl"

/**
 * 下载一个 OfficialCDN Package 需要以下步骤：
 * 要求每一步都可以回显
 * 解析：
 *  知道分包的 UUID
 *  获取分包文件的 Chunk
 * 下载：
 *  下载每一个 Chunk
 *  合并 Chunk 为一个文件
 * 安装
 */
class OfficialCDNPackageDownloader(context: Context) {
    private val downloadDir = context.filesDir.resolve("downloads")
        get() = field.also { if (!it.exists()) it.mkdirs() }

    private val downloadPacksDir = downloadDir.resolve("packs")
        get() = field.also { if (!it.exists()) it.mkdirs() }

    data class DownloadResult(
        val packageZipFile: File,
        val graphicsFile: File
    )

    fun download(pack: OfficialCDNPackage): PackageDownloadTask {
        return PackageDownloadTask(pack)
    }

    inner class PackageDownloadTask(private val pack: OfficialCDNPackage) {
        private val scope = CoroutineScope(Dispatchers.IO)

        // Chunk index, progress
        private val _progress = MutableStateFlow<Pair<Long, Long>>(0L to -1L)
        val progress = _progress

        private lateinit var job: Deferred<DownloadResult>

        private val zipFile = downloadPacksDir.resolve("${pack.uuid}.zip")
        private val graphicsFile = downloadDir.resolve("${pack.uuid}_graphics.zip")

        init {
            start()
        }

        private fun start() {
            job = scope.async {
                // Graphics
                downloadGraphic()
                val files = downloadChunks()
                // Merge
                mergeFiles(files)

                return@async DownloadResult(zipFile, graphicsFile)
            }
        }

        private suspend fun downloadGraphic() {
            val task = FileDownloader.newTask(pack.graphicsUrl)
            graphicsFile.outputStream().use {
                task.setOutput(it)
                task.connect()
                task.download()
            }
        }

        private suspend fun downloadChunks(): List<File> = coroutineScope {
            // Download to files
            val (tasks, totalSize) = run {
                val sum = AtomicLong(0L)
                pack.chunks.map { chunk ->
                    async {
                        val task = FileDownloader.newTask(chunk.url)
                        val size = task.connect()
                        sum.getAndUpdate { it + size }
                        task
                    }
                }.awaitAll() to sum.get()
            }

            val downloaded = LongArray(tasks.size) { 0L }
            val channel = Channel<Pair<Int, Long>>(Channel.UNLIMITED)

            launch {
                for ((index, value) in channel) {
                    downloaded[index] = value
                    _progress.emit(downloaded.sum() to totalSize)
                }
            }

            // TODO: Optimize concurrent code
            val result = tasks.mapIndexed { index, task ->
                async(Dispatchers.IO) {
                    val file = File(zipFile.absolutePath + ".part_${index}")
                    val output = file.outputStream()
                    task.setOutput(output)

                    val job = launch {
                        task.progress.collect {
                            channel.send(index to it)
                        }
                    }

                    try {
                        task.download()
                    } finally {
                        job.cancelAndJoin()
                        output.close()
                    }
                    file
                }
            }.awaitAll()

            channel.close()

            return@coroutineScope result
        }

        private suspend fun mergeFiles(files: List<File>) = withContext(Dispatchers.IO) {
            zipFile.outputStream().use { output ->
                files.forEach {
                    output.write(it.readBytes())
                    it.delete()
                }
            }
        }

        suspend fun await(): DownloadResult = job.await()
    }
}

