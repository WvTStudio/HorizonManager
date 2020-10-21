package org.wvt.horizonmgr.ui.community

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.platform.setContent

class CommunityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Community(onClose = ::close)
                }
            }
        }
        /*// TODO 现在是临时解决方案
        setContentView(R.layout.community_layout)
        findViewById<ComposeView>(R.id.compose_view).setContent {
            AndroidHorizonManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Community(onClose = ::close)
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
        }*/
    }

    private fun close() {
        finish()
    }
}