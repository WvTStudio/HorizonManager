package org.wvt.horizonmgr.ui.community

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animate
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.ExperimentalKeyInput
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalKeyInput::class)
@Composable
fun Community(onClose: () -> Unit) {
    val backgroundColor = MaterialTheme.colors.background
    var loading by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf<Float>(0f) }
    val context = ContextAmbient.current

    /*val webView = remember {
        WebView(context).apply {
            Log.d("Community", "WebView apply")
            settings.apply {
                javaScriptEnabled = true
                setSupportZoom(false)
                javaScriptCanOpenWindowsAutomatically = true
                domStorageEnabled = true
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progress = newProgress / 100f
                }
            }
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    loading = true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    loading = false
                }
            }
            setOnKeyListener { v, keyCode, event ->
                val webview = v as WebView
                if (event.action == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
                        webview.goBack()
                        return@setOnKeyListener true
                    }
                }
                return@setOnKeyListener false
            }
            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimetype)
                    val cookies = CookieManager.getInstance().getCookie(url)
                    addRequestHeader("cookie", cookies)
                    addRequestHeader("User-Agent", userAgent)
                    setDescription("正在下载...")
                    setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        URLUtil.guessFileName(url, contentDisposition, mimetype)
                    )
                }
                val dm =
                    context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.openDownloadedFile(dm.enqueue(request))
            }
            loadUrl("https://forum.adodoz.cn")
        }
    }*/

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = {
            Text("Inner Core 中文社区")
        }, navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close)
            }
        }, backgroundColor = MaterialTheme.colors.surface, actions = {
            Crossfade(current = loading) {
                if (it) CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    progress = animate(progress)
                )
            }
        })
/*        AndroidView(
            modifier = Modifier.fillMaxSize().keyInputFilter { true },
            viewBlock = { webView },
            update = {
                it.setBackgroundColor(backgroundColor.toArgb())
            }
        )*/
    }
}