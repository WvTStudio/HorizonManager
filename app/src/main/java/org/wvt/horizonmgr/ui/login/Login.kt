package org.wvt.horizonmgr.ui.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.AnimationConstants.Infinite
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadVectorResource
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.service.WebAPI

private val rotation = FloatPropKey()
private val rotationDefinition = transitionDefinition<Int> {
    state(0) { this[rotation] = 0f }
    state(1) { this[rotation] = 360f }

    transition(0 to 1) {
        rotation using infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing
            )
        )
    }
}

private val reverseRotation = FloatPropKey()
private val reverseRotationDefinition = transitionDefinition<Int> {
    state(0) { this[reverseRotation] = 360f }
    state(1) { this[reverseRotation] = 0f }
    transition(0 to 1) {
        reverseRotation using infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing
            )
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun Login(
    vm: LoginViewModel,
    onLoginSuccess: (WebAPI.UserInfo) -> Unit,
    onCancel: () -> Unit
) {
    val fabState by vm.fabState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var screen by remember { mutableStateOf(0) }
    val gearResource = loadVectorResource(id = R.drawable.ic_gear_full).resource.resource

    val rotateGear = transition(definition = rotationDefinition, initState = 0, toState = 1)

    Box(Modifier.fillMaxSize()) {
        // Gear Animations
        gearResource?.let {
            Box(
                Modifier.size(256.dp)
                    .offset(x = 128.dp)
                    .graphicsLayer(rotationZ = rotateGear[rotation])
                    .align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().paint(
                        painter = rememberVectorPainter(it),
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface.copy(0.12f))
                    )
                )
            }
            Box(
                Modifier.size(256.dp)
                    .offset(x = (-128).dp)
                    .graphicsLayer(rotationZ = rotateGear[rotation])
                    .align(Alignment.BottomStart)
            ) {
                Box(
                    Modifier.fillMaxSize().paint(
                        painter = rememberVectorPainter(it),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface.copy(0.12f))
                    )
                )
            }
        }

        // Back button
        IconButton(
            modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
            onClick = {
                if (screen == 1)
                    screen = 0
                else if (screen == 0)
                    onCancel()
            }) {
            Icon(Icons.Filled.ArrowBack)
        }

        // Login Page
        AnimatedVisibility(
            visible = screen == 0,
            enter = fadeIn() + slideInHorizontally({ -40 }),
            exit = fadeOut() + slideOutHorizontally({ -40 })
        ) {
            LoginPage(onLoginClicked = { account, password ->
                vm.login(account, password, snackbarHostState, onLoginSuccess)
//                login(scope, account, password)
            }, onRegisterRequested = {
                screen = 1
            }, fabState = fabState)
        }

        // RegisterPage
        AnimatedVisibility(
            visible = screen == 1,
            enter = fadeIn() + slideInHorizontally({ 40 }),
            exit = fadeOut() + slideOutHorizontally({ 40 })
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
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            hostState = snackbarHostState
        )
    }
}