package org.wvt.horizonmgr.ui.login

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.dependenciesViewModel

private val rotationSpec = InfiniteRepeatableSpec<Float>(
    tween(durationMillis = 5000, easing = LinearEasing), RepeatMode.Restart
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Login(
    onLoginSuccess: (account: String, avatar: String?, name: String, uid: String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current as ComponentActivity
    val vm = dependenciesViewModel<LoginViewModel>()
    val fabState by vm.fabState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var screen by remember { mutableStateOf(0) }
    val gearResource = painterResource(id = R.drawable.ic_gear_full)

    val gearRotation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = rotationSpec
    )

    DisposableEffect(Unit) {
        context.onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (screen == 1) {
                    screen = 0
                } else if (screen == 0) {
                    onCancel()
                }
            }
        })
        onDispose {
            // TODO: 2021/2/7 添加 Dispose 逻辑
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Gear Animations
        Box(
            Modifier
                .size(256.dp)
                .offset(x = 128.dp)
                .align(Alignment.TopEnd)
        ) {
            Canvas(modifier = Modifier.matchParentSize(), onDraw = {
                rotate(gearRotation) {
                    with(gearResource) {
                        draw(Size(256.dp.toPx(), 256.dp.toPx()))
                    }
                }
            })
        }
        Box(
            Modifier
                .size(256.dp)
                .offset(x = (-128).dp)
                .graphicsLayer(rotationZ = gearRotation)
                .align(Alignment.BottomStart)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .paint(
                        painter = gearResource,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface.copy(0.12f))
                    )
            )
        }

        // Back button
        IconButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp),
            onClick = {
                if (screen == 1)
                    screen = 0
                else if (screen == 0)
                    onCancel()
            }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
        }

        // Login Page
        AnimatedVisibility(
            visible = screen == 0,
            initiallyVisible = true,
            enter = remember { fadeIn() + slideInHorizontally({ -80 }) },
            exit = remember { fadeOut() + slideOutHorizontally({ -80 }) }
        ) {
            LoginPage(onLoginClicked = { account, password ->
                vm.login(account, password, snackbarHostState, onLoginSuccess)
            }, onRegisterRequested = {
                screen = 1
            }, fabState = fabState)
        }

        // RegisterPage
        AnimatedVisibility(
            visible = screen == 1,
            initiallyVisible = false,
            enter = remember { fadeIn() + slideInHorizontally({ 80 }) },
            exit = remember { fadeOut() + slideOutHorizontally({ 80 }) }
        ) {
            RegisterPage(
                fabState = fabState,
                onRegisterRequest = { u, e, p, c ->
                    vm.register(u, e, p, c, snackbarHostState) { _, _, _ ->
                        screen = 0
                    }
                },
            )
        }

        SnackbarHost(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            hostState = snackbarHostState
        )
    }
}