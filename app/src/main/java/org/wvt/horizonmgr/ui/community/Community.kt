package org.wvt.horizonmgr.ui.community

import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.utils.longSizeToString

@Composable
internal fun Community(onClose: () -> Unit) {
    val vm = dependenciesViewModel<CommunityViewModel>()
    val newtask by vm.newTask.collectAsState()

    var loading by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0f) }
    val context = AmbientContext.current

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .zIndex(
                        with(AmbientDensity.current) { 4.dp.toPx() }
                    )
                    .shadow(4.dp),
                title = {
                    Text("Inner Core 中文社区")
                }, navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, "关闭")
                    }
                }, backgroundColor = MaterialTheme.colors.surface, actions = {
                    Crossfade(current = loading) {
                        if (it) CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            progress = animateFloatAsState(progress).value
                        )
                    }
                }
            )
            WebViewCompose(
                modifier = Modifier.weight(1f),
                onProgressChanged = { progress = it },
                onStateChanged = { loading = it },
                newDownloadTask = { url, userAgent, contentDisposition, mimetype, contentLength ->
                    vm.newTask(context, url, userAgent, contentDisposition, mimetype, contentLength)
                }, onClose = onClose
            )
        }
        newtask?.let { it ->
            // New Download Task Dialog
            Dialog(onDismissRequest = { vm.clearDownloadTask() }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 24.dp
                ) {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            modifier = Modifier.padding(top = 24.dp),
                            text = "新下载任务", style = MaterialTheme.typography.h6
                        )
                        Text(
                            modifier = Modifier.padding(top = 16.dp),
                            text = "文件名: " + it.filename
                        )
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = "地址: " + it.url
                        )
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = "大小: " + longSizeToString(it.size)
                        )
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { vm.download(context) }) {
                                Text("下载")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WebViewCompose(
    modifier: Modifier = Modifier,
    onProgressChanged: (progress: Float) -> Unit,
    onStateChanged: (loading: Boolean) -> Unit,
    newDownloadTask: (url: String, userAgent: String, contentDisposition: String, mimeType: String, contentLength: Long) -> Unit,
    onClose: () -> Unit
) {
    val backgroundColor = MaterialTheme.colors.background
    val backpack = (AmbientContext.current as ComponentActivity).onBackPressedDispatcher

    AndroidView(
        modifier = modifier,
        viewBlock = {
            WebView(it).apply {
                settings.apply {
                    javaScriptEnabled = true
                    setSupportZoom(false)
                    javaScriptCanOpenWindowsAutomatically = true
                    domStorageEnabled = true
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgressChanged(newProgress / 100f)
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(
                        view: WebView?,
                        url: String?,
                        favicon: Bitmap?
                    ) {
                        onStateChanged(true)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        onStateChanged(false)
                    }
                }
                setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                    newDownloadTask(url, mimetype, userAgent, contentDisposition, contentLength)
                }
                loadUrl("https://forum.adodoz.cn")
            }.also { view ->
                backpack.addCallback(object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (view.canGoBack()) {
                            view.goBack()
                        } else {
                            onClose()
                        }
                    }
                })
            }
        },
        update = {
            it.setBackgroundColor(backgroundColor.toArgb())
        }
    )
}