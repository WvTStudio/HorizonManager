package org.wvt.horizonmgr.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.components.FabState
import org.wvt.horizonmgr.ui.components.StateFab
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginPage(
    onLoginClicked: (account: String, password: String) -> Unit,
    onRegisterRequested: () -> Unit,
    fabState: FabState
) {
    val (accountFocus, passwordFocus) = FocusRequester.createRefs()

    var account by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    var password by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
        ) {
            // Title
            Text(
                text = stringResource(R.string.login_screen_login_title) , color = MaterialTheme.colors.primary, fontSize = 64.sp
            )
            // Subtitle
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = stringResource(R.string.login_screen_login_subtitle),
                )
            }
            // Form
            Column(
                modifier = Modifier.wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                // Account/Email
                TextField(
                    modifier = Modifier.focusOrder(accountFocus) {
                        next = passwordFocus
                        down = passwordFocus
                    },
                    value = account,
                    onValueChange = { account = it },
                    label = { Text( stringResource(R.string.login_screen_login_input_account_or_email)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        passwordFocus.requestFocus()
                    })
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Password
                TextField(
                    modifier = Modifier.focusOrder(passwordFocus) {
                        previous = accountFocus
                        up = accountFocus
                    },
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.login_screen_login_input_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        passwordFocus.freeFocus()
                        onLoginClicked(account.text, password.text)
                    })
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Login Button
                StateFab(state = fabState, onClicked = {
                    onLoginClicked(account.text, password.text)
                })
                // Register Button
                TextButton(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = onRegisterRequested
                ) {
                    Text(stringResource(R.string.login_screen_login_button_goto_register))
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Surface(color = MaterialTheme.colors.background) {
            LoginPage(
                onLoginClicked = { account, password ->  },
                onRegisterRequested = { },
                fabState = FabState.TODO
            )
        }
    }
}
