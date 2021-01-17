package org.wvt.horizonmgr.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wvt.horizonmgr.ui.components.FabState
import org.wvt.horizonmgr.ui.components.StateFab

@Composable
fun LoginPage(
    onLoginClicked: (account: String, password: String) -> Unit,
    onRegisterRequested: () -> Unit,
    fabState: FabState
) {
    val (accountFocus, passwordFocus) = FocusRequester.createRefs()
    var account by savedInstanceState(saver = TextFieldValue.Saver) {
        TextFieldValue()
    }
    var password by savedInstanceState(saver = TextFieldValue.Saver) {
        TextFieldValue()
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.wrapContentSize().align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "登录", color = MaterialTheme.colors.primary, fontSize = 64.sp
            )
            Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "使用 InnerCore 中文社区账号登录",
                )
            }
            Column(
                modifier = Modifier.wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                TextField(
                    modifier = Modifier.focusOrder(accountFocus) {
                        next = passwordFocus
                        down = passwordFocus
                    },
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("用户名/邮箱") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    onImeActionPerformed = { _, _ ->
                        passwordFocus.requestFocus()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    modifier = Modifier.focusOrder(passwordFocus) {
                        previous = accountFocus
                        up = accountFocus
                    },
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    onImeActionPerformed = { _, softwareKeyboardController ->
                        softwareKeyboardController?.hideSoftwareKeyboard()
                        passwordFocus.freeFocus()
                        onLoginClicked(account.text, password.text)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                StateFab(state = fabState, onClicked = {
                    onLoginClicked(account.text, password.text)
                })
                TextButton(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = onRegisterRequested
                ) {
                    Text("注册")
                }
            }
        }
    }
}

