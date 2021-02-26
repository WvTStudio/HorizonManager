package org.wvt.horizonmgr.ui.community

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.CookieManager
import android.webkit.URLUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun interface ContextFactory {
    fun get(): Context
}

class CommunityViewModel(
    private val contextFactory: ContextFactory
) : ViewModel() {
    companion object {
        const val TAG = "CommunityVM"
    }

    data class DownloadTask(
        val filename: String,
        val url: String,
        val size: Long,
        val mimetype: String,
        val userAgent: String,
        val contentDisposition: String
    )

    val newTask = MutableStateFlow<DownloadTask?>(null)

    fun newTask(
        url: String,
        mimetype: String,
        userAgent: String,
        contentDisposition: String,
        contentLength: Long
    ) {
        Log.i(
            TAG,
            "url: $url, mimetype: $mimetype, userAgent: $userAgent, contentDisposition: $contentDisposition, contentLength: $contentLength"
        )

        viewModelScope.launch {
            newTask.emit(DownloadTask(
                filename = URLUtil.guessFileName(url, contentDisposition, mimetype),
                url = url,
                size = contentLength,
                userAgent = userAgent,
                contentDisposition = contentDisposition,
                mimetype = mimetype
            ))
        }
    }

    fun download() {
        val task = newTask.value ?: return
        viewModelScope.launch {
            val context = contextFactory.get()
            val request = DownloadManager.Request(Uri.parse(task.url)).apply {
                setMimeType(task.mimetype)
                val cookies = CookieManager.getInstance().getCookie(task.url)
                addRequestHeader("cookie", cookies)
                addRequestHeader("User-Agent", task.userAgent)
                setDescription("正在下载...")
                setTitle(task.filename)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, task.filename)
            }

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            newTask.emit(null)
        }
    }

    fun clearDownloadTask() {
        viewModelScope.launch {
            newTask.emit(null)
        }
    }
}