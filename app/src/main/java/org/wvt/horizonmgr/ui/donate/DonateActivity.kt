package org.wvt.horizonmgr.ui.donate

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.coil.rememberCoilPainter
import org.wvt.horizonmgr.defaultViewModelFactory
import org.wvt.horizonmgr.ui.theme.AndroidDonateTheme

class DonateActivity : AppCompatActivity() {
    private val vm by viewModels<DonateViewModel> { defaultViewModelFactory }

    @SuppressLint("ResourceType")
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val hostState = SnackbarHostState()
            val mDonates by vm.donates.collectAsState()
            var showQR by remember { mutableStateOf(0) }

            LaunchedEffect(vm) { vm.refresh() }

            AndroidDonateTheme {
                Box(Modifier.fillMaxSize()) {
                    if (showQR != 0) Dialog(onDismissRequest = { showQR = 0 }) {
                        Card(
                            Modifier
                                .width(280.dp)
                                .wrapContentHeight(), elevation = 16.dp) {
                            val qrUrl = if (showQR == 1) vm.alipayQrCodeURL else vm.wechatQrCodeURL
                            Column(modifier = Modifier.padding(16.dp)) {
                                Image(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f),
                                    painter = rememberCoilPainter(request = qrUrl, fadeIn = true),
                                    contentDescription = "QR"
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    text = "请务必备注昵称，感谢您的支持",
                                    color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Surface(
                        color = MaterialTheme.colors.background
                    ) {
                        Donate(
                            donates = mDonates,
                            onAlipayClicked = { showQR = 1 },
                            onWechatPayClicked = { showQR = 2 },
                            onClose = { finish() }
                        )
                    }
                    SnackbarHost(hostState = hostState, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }
}