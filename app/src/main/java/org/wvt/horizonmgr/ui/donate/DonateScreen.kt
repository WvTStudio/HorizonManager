package org.wvt.horizonmgr.ui.donate

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@Composable
fun DonateScreen(
    vm: DonateViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    val hostState = SnackbarHostState()
    val mDonates by vm.donates.collectAsState()
    var showQR by remember { mutableStateOf(0) }

    LaunchedEffect(vm) { vm.refresh() }

    Box(Modifier.fillMaxSize()) {
        if (showQR != 0 ) QRDialog(
            onDismissRequest = { showQR = 0 },
            qrUrl = if (showQR == 1) vm.alipayQrCodeURL else vm.wechatQrCodeURL
        )
        Surface(color = MaterialTheme.colors.background) {
            Donate(
                donates = mDonates,
                onAlipayClicked = { showQR = 1 },
                onWechatPayClicked = { showQR = 2 },
                onClose = onClose
            )
        }
        SnackbarHost(hostState = hostState, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun QRDialog(
    onDismissRequest: () -> Unit,
    qrUrl: String
) {
    Dialog(onDismissRequest = onDismissRequest) {
        QRDialogContent(qrUrl)
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun QRDialogContent(qrUrl: String) {
    Card(
        Modifier
            .width(280.dp)
            .wrapContentHeight(), elevation = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                painter = rememberImagePainter(qrUrl, builder = { crossfade(true) }),
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

@Preview
@Composable
private fun QRDialogPreview() {
    PreviewTheme {
        QRDialogContent(qrUrl = "")
    }
}