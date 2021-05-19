package org.wvt.horizonmgr.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class PackageDownloadWorker(
    context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        TODO()
    }
}