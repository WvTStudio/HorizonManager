package org.wvt.horizonmgr.ui.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.AnimationConstants.Infinite
import androidx.compose.foundation.Icon
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.drawLayer
import androidx.compose.ui.focus.ExperimentalFocus
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadVectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.ui.WebAPIAmbient
import org.wvt.horizonmgr.ui.components.FabState
import org.wvt.horizonmgr.ui.theme.HorizonManagerTheme

private val rotation = FloatPropKey()
private val rotationDefinition = transitionDefinition<Int> {
    state(0) { this[rotation] = 0f }
    state(1) { this[rotation] = 360f }

    transition(0 to 1) {
        rotation using repeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing
            ),
            iterations = Infinite
        )
    }
}

private val reverseRotation = FloatPropKey()
private val reverseRotationDefinition = transitionDefinition<Int> {
    state(0) { this[reverseRotation] = 360f }
    state(1) { this[reverseRotation] = 0f }
    transition(0 to 1) {
        reverseRotation using repeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing
            ),
            iterations = Infinite
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class, ExperimentalFocus::class)
@Composable
fun Login(
    onLoginSuccess: (WebAPI.UserInfo) -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var fabState by remember { mutableStateOf(FabState.TODO) }

    val snackbarHostState = remember { SnackbarHostState() }
    var screen by remember { mutableStateOf(0) }
    val gearResource = loadVectorResource(id = R.drawable.ic_gear_full).resource.resource

    val rotateGear = transition(definition = rotationDefinition, initState = 0, toState = 1)

    val webApi = WebAPIAmbient.current

    fun login(scope: CoroutineScope, account: String, password: String) {
        scope.launch {
            fabState = FabState.LOADING
            val userInfo = try {
                webApi.login(account, password)
            } catch (e: WebAPI.WebAPIException) {
                fabState = FabState.FAILED
                launch {
                    snackbarHostState.showSnackbar(e.message, "确定")
                    fabState = FabState.TODO
                }
                return@launch
            } catch (e: Exception) {
                e.printStackTrace()
                fabState = FabState.FAILED
                launch {
                    snackbarHostState.showSnackbar("未知错误，请稍后重试", "确定")
                    fabState = FabState.TODO
                }
                return@launch
            }
            fabState = FabState.SUCCEED
            snackbarHostState.showSnackbar("登录成功", "前往主页")
            withContext(Dispatchers.Main) {
                onLoginSuccess(userInfo)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        gearResource?.let {
            Box(
                Modifier.size(256.dp)
                    //                    .background(Color.Red)
                    .offset(x = 128.dp)
                    .drawLayer(rotationZ = rotateGear[rotation])
                    .align(Alignment.TopEnd)
            ) {
                Box(
                    Modifier.fillMaxSize().paint(
                        painter = VectorPainter(it),
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(
                            MaterialTheme.colors.onSurface.copy(
                                0.12f
                            )
                        )
                    )
                ) {}
            }
            Box(
                Modifier.size(256.dp)
                    .offset(x = (-128).dp)
                    .drawLayer(rotationZ = rotateGear[rotation])
                    .align(Alignment.BottomStart)
            ) {
                Box(
                    Modifier.fillMaxSize().paint(
                        painter = VectorPainter(it),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(
                            MaterialTheme.colors.onSurface.copy(
                                0.12f
                            )
                        )
                    )
                ) {}
            }
        }
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        if (screen == 1) {
                            screen = 0
                        } else if (screen == 0) onCancel()
                    }) {
                        Icon(Icons.Filled.ArrowBack)
                    }
                },
                title = {},
                backgroundColor = Color.Transparent,
                elevation = 0.dp
            )

            Box(Modifier.fillMaxSize()) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = screen == 0,
                    enter = fadeIn() + slideInHorizontally({ -40 }),
                    exit = fadeOut() + slideOutHorizontally({ -40 })
                ) {
                    LoginPage(onLoginClicked = { account, password ->
                        login(scope, account, password)
                    }, onRegisterRequested = {
                        fabState = FabState.TODO
                        screen = 1
                    }, fabState = fabState)
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = screen == 1,
                    enter = fadeIn() + slideInHorizontally({ 40 }),
                    exit = fadeOut() + slideOutHorizontally({ 40 })
                ) {
                    RegisterPage(onSuccess = { uid, e, p ->
                        fabState = FabState.TODO
                        screen = 0
                    })
                }
            }
        }
        SnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            hostState = snackbarHostState
        )
    }
}


@Preview
@Composable
private fun LoginPreview() {
    HorizonManagerTheme {
        Surface {
            Login(onLoginSuccess = {}, onCancel = {})
        }
    }
}