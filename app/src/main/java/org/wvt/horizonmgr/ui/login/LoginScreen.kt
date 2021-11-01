package org.wvt.horizonmgr.ui.login

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R

private const val TAG = "LoginScreen"

@Composable
private fun AnimationGear(
    modifier: Modifier
) {
    val gearRotation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = InfiniteRepeatableSpec(
            tween(durationMillis = 5000, easing = LinearEasing),
            RepeatMode.Restart
        )
    )
    val gearResource = ImageVector.vectorResource(id = R.drawable.ic_gear_full)
    Box(modifier.rotate(gearRotation)) {
        Image(
            modifier = Modifier.fillMaxSize(),
            imageVector = gearResource,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface.copy(0.12f))
        )
    }
}

private enum class Screen {
    LOGIN, REGISTER
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (account: String, avatar: String?, name: String, uid: String) -> Unit,
    onCancel: () -> Unit
) {
    val fabState by viewModel.fabState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var screen by rememberSaveable { mutableStateOf(Screen.LOGIN) }

    fun back() {
        when (screen) {
            Screen.REGISTER -> screen = Screen.LOGIN
            Screen.LOGIN -> onCancel()
        }
    }

    BackHandler(onBack = ::back)

    Box(Modifier.fillMaxSize()) {
        AnimationGear(
            Modifier
                .size(256.dp)
                .offset(x = 128.dp)
                .align(Alignment.TopEnd)
        )
        AnimationGear(
            Modifier
                .size(256.dp)
                .offset(x = (-128).dp)
                .align(Alignment.BottomStart)
        )

        // Back button
        IconButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp),
            onClick = ::back
        ) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = stringResource(R.string.login_screen_back_desc)
            )
        }

        // Login Page
        AnimatedVisibility(
            visible = screen == Screen.LOGIN,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -80 }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -80 })
        ) {
            LoginPage(onLoginClicked = { account, password ->
                viewModel.login(account, password, snackbarHostState, onLoginSuccess)
            }, onRegisterRequested = {
                screen = Screen.REGISTER
            }, fabState = fabState)
        }

        // RegisterPage
        AnimatedVisibility(
            visible = screen == Screen.REGISTER,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { 80 }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { 80 })
        ) {
            RegisterPage(
                fabState = fabState,
                onRegisterRequest = { u, e, p, c ->
                    viewModel.register(u, e, p, c, snackbarHostState) { _, _, _ ->
                        screen = Screen.LOGIN
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