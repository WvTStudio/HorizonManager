package org.wvt.horizonmgr.legacyservice

import kotlinx.coroutines.channels.ReceiveChannel

interface MultiThreadDownloadTask {
    sealed class State {
        object Loading : State()
        data class Downloading(val current: Long, val total: Long) : State()
        object Verifying : State()
        class Finished(val size: Long) : State()
        class Error(val error: Throwable) : State()
    }

    class Chunks(
        val chunkCount: Int,
        val states: List<ReceiveChannel<State>>
    )

    suspend fun await(): Long
    suspend fun cancel()
    suspend fun getState(): ReceiveChannel<State>
    suspend fun getChunks(): Chunks
}