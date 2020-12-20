package org.wvt.horizonmgr.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface ProgressDeferred<P, R> {
    suspend fun progressChannel(): ReceiveChannel<P>
    suspend fun await(): R
}

fun <P, R> CoroutineScope.progressAsync(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.(progressChannel: SendChannel<P>) -> R
): ProgressDeferred<P, R> {
    return object : ProgressDeferred<P, R> {
        private val scope = this@progressAsync + coroutineContext
        private val channel = Channel<P>(Channel.UNLIMITED)
        private val job = scope.async {
            block(channel)
        }

        override suspend fun await(): R = job.await()
        override suspend fun progressChannel(): ReceiveChannel<P> = channel
    }
}