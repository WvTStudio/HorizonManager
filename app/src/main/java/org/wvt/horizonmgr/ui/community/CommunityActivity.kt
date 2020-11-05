package org.wvt.horizonmgr.ui.community

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.components.Background
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class CommunityActivity : AppCompatActivity() {
    private lateinit var dm: DownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO 使用纯 Compose 方案
        setContentView(R.layout.community_layout)

        findViewById<ComposeView>(R.id.compose_view).setContent {
            AndroidHorizonManagerTheme {
                Background {
                    Column(Modifier.fillMaxSize()) {
                        TopAppBar(
                            title = { Text("Inner Core 中文社区") },
                            navigationIcon = {
                                IconButton(onClick = ::close) {
                                    Icon(Icons.Filled.Close)
                                }
                            },
                            backgroundColor = MaterialTheme.colors.surface
                        )
                    }
                }
            }
        }

        findViewById<WebView>(R.id.webview).apply {
            Log.d("Community", "WebView apply")
            settings.apply {
                javaScriptEnabled = true
                setSupportZoom(false)
                javaScriptCanOpenWindowsAutomatically = true
                domStorageEnabled = true
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
//                    progress = newProgress / 100f
                }
            }
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//                    loading = true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
//                    loading = false
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
                    setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                    setDescription("正在下载...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, null)
                }
                dm.openDownloadedFile(dm.enqueue(request))
            }
            loadUrl("https://forum.adodoz.cn")
        }

        dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    private fun close() {
        finish()
    }
}