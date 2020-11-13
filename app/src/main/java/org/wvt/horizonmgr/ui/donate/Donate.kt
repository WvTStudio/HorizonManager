package org.wvt.horizonmgr.ui.donate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.node.ExperimentalLayoutNodeApi
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import org.wvt.horizonmgr.R
import kotlin.random.Random

private val alipayColor = Color(0xFF1678FF)
private val wechatColor = Color(0xFF19AD19)

private data class DonateItem(
    val name: String,
    val size: TextUnit
)

@OptIn(ExperimentalLayout::class, ExperimentalAnimationApi::class)
@Composable
fun Donate(
    donates: Set<DonateViewModel.DonateItem>,
    onClose: () -> Unit,
    onAlipayClicked: () -> Unit,
    onWechatPayClicked: () -> Unit
) {
    var displayDialog by remember { mutableStateOf(false) }
    val light = !isSystemInDarkTheme()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // Alipay
            Surface(
                color = if (light) alipayColor else MaterialTheme.colors.background,
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .clickable(onClick = onAlipayClicked, indication = null)
            ) {
                Box(Modifier.fillMaxSize()) {
                    Icon(
                        modifier = Modifier.align(Alignment.Center),
                        asset = vectorResource(id = R.drawable.ic_alipay),
                        tint = if (light) Color.White else alipayColor
                    )
                }
            }
            // Wechat
            Surface(
                color = if (light) wechatColor else MaterialTheme.colors.background,
                modifier = Modifier.weight(1f).fillMaxWidth().clickable(
                    onClick = onWechatPayClicked,
                    indication = null
                )
            ) {
                Box(Modifier.fillMaxSize()) {
                    Icon(
                        modifier = Modifier.align(Alignment.Center),
                        asset = vectorResource(id = R.drawable.ic_wechatpay),
                        tint = if (light) Color.White else wechatColor
                    )
                }
            }
        }
        Column {
            TopAppBar(
                title = { Text("选择捐赠方式") },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(asset = Icons.Filled.ArrowBack) }
                },
                elevation = 0.dp,
                backgroundColor = Color.Transparent,
                contentColor = Color.White
            )
            AnimatedVisibility(
                visible = donates.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                RandomPlaceLayout(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                    donates.forEach {
                        Text(
                            fontSize = it.size,
                            text = it.name,
                            color = if (light) Color.White.copy(0.3f)
                            else MaterialTheme.colors.onSurface.copy(0.3f)
                        )
                    }
                }
            }
        }
        Row(Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            TextButton(onClick = { displayDialog = true }) {
                Text(
                    text = "关于",
                    color = if (light) Color.White else MaterialTheme.colors.primary
                )
            }
        }

        if (displayDialog) {
            AlertDialog(
                onDismissRequest = { displayDialog = false },
                title = { Text("关于捐赠") },
                text = {
                    Text(
                        "本应用由几位爱好者开发维护，皆为在校学生。" +
                                "您的捐款将用于支付服务器费用，如有多余将用来改善生活。\n" +
                                "请在捐赠时务必留下您的名称，感谢您的支持！"
                    )
                },
                confirmButton = { TextButton(onClick = { displayDialog = false }) { Text("关闭") } }
            )
        }
    }
}

@OptIn(ExperimentalLayoutNodeApi::class)
@Composable
fun RandomPlaceLayout(modifier: Modifier, children: @Composable () -> Unit) {
    val random = Random
    Layout(
        modifier = modifier,
        children = children
    ) { list: List<Measurable>, constraints: Constraints ->
        val childrenConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = list.map {
            it.measure(childrenConstraints)
        }
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.fastForEach {
                val x = random.nextInt(0, constraints.maxWidth - it.width)
                val y = random.nextInt(0, constraints.maxHeight - it.height)
                it.place(x, y)
            }
        }
    }
}