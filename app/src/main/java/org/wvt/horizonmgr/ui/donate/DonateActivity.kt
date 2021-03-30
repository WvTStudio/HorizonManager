package org.wvt.horizonmgr.ui.donate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.defaultViewModelFactory
import org.wvt.horizonmgr.ui.theme.AndroidDonateTheme
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class DonateActivity : AppCompatActivity() {
    companion object {
        const val aliPayCode = "fkx18184vir1w8crfl6vsa9"
    }

    private val vm by viewModels<DonateViewModel> { defaultViewModelFactory }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val hostState = SnackbarHostState()
            val scope = rememberCoroutineScope()
            var job by remember { mutableStateOf<Job?>(null) }
            val mDonates by vm.donates.collectAsState()

            DisposableEffect(vm) {
                vm.refresh()
                onDispose {
                    // TODO: 2021/2/6  添加 cancel
                }
            }

            AndroidDonateTheme {
                Box(Modifier.fillMaxSize()) {
                    Surface(
                        color = MaterialTheme.colors.background
                    ) {
                        Donate(
                            donates = mDonates,
                            onAlipayClicked = {
                                if (job?.isActive == true) return@Donate
                                job = scope.launch {
                                    try {
                                        startAlipay()
                                    } catch (e: Exception) {
                                        hostState.showSnackbar("支付宝启动失败", "确定")
                                        return@launch
                                    }
                                }
                            }, onWechatPayClicked = {
                                if (job?.isActive == true) return@Donate
                                job = scope.launch {
                                    try {
                                        saveQrCode()
                                    } catch (e: Exception) {
                                        hostState.showSnackbar("收款码保存失败", actionLabel = "确定")
                                        return@launch
                                    }
                                    hostState.showSnackbar("收款码已保存到相册，即将启动微信", "确定")
                                    try {
                                        startWechatPay()
                                    } catch (e: Exception) {
                                        hostState.showSnackbar("微信启动失败", actionLabel = "确定")
                                        return@launch
                                    }
                                }
                            }, onClose = {
                                finish()
                            })
                    }
                    SnackbarHost(hostState = hostState, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }

    private suspend fun startAlipay() = withContext(Dispatchers.Main) {
        val url =
            "intent://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F${aliPayCode}%3F_s%3Dweb-other&_t=1472443966571#Intent;scheme=alipayqr;package=com.eg.android.AlipayGphone;end"
        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
        startActivity(intent)
    }

    private suspend fun saveQrCode() = withContext(Dispatchers.IO) {
        val qrPath = Environment.getExternalStorageDirectory().resolve("DCIM")
            .resolve("Screenshots").also { it.mkdirs() }
            .resolve("hz_wechat_donate_qr_c.png")

        if (!qrPath.exists()) {
            resources.openRawResource(R.raw.wechatpay_qr).use { imgInput ->
                qrPath.outputStream().use { fileOutput ->
                    imgInput.copyTo(fileOutput)
                }
            }
            // brocast media save
            val intent = Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE")
            val uri = Uri.fromFile(qrPath)
            intent.data = uri
            sendBroadcast(intent)
        }
    }


    private suspend fun startWechatPay() = withContext(Dispatchers.Main) {
        val intent = Intent("com.tencent.mm.action.BIZSHORTCUT")
        intent.setPackage("com.tencent.mm")
        intent.putExtra("LauncherUI.From.Scaner.Shortcut", true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}