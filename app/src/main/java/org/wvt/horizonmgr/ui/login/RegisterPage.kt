package org.wvt.horizonmgr.ui.login

import androidx.compose.foundation.ScrollableColumn
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RegisterPage(
    fabState: FabState,
    onRegisterRequest: (username: String, email: String, password: String, confirmPassword: String) -> Unit
) {
    val (usernameFocus, emailFocus, passwordFocus, confirmFocus) = FocusRequester.createRefs()
    var username by savedInstanceState(saver = TextFieldValue.Saver) { TextFieldValue() }
    var email by savedInstanceState(saver = TextFieldValue.Saver) { TextFieldValue() }
    var password by savedInstanceState(saver = TextFieldValue.Saver) { TextFieldValue() }
    var confirmPassword by savedInstanceState(saver = TextFieldValue.Saver) { TextFieldValue() }

    Box(Modifier.fillMaxSize()) {
        ScrollableColumn(
            modifier = Modifier
                .wrapContentWidth()
                .fillMaxHeight()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center
        ) {
            Text("注册", color = MaterialTheme.colors.primary, fontSize = 64.sp)
            Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "注册到 InnerCore 中文社区 ",
                )
            }
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                TextField(
                    modifier = Modifier.focusOrder(usernameFocus) {
                        next = emailFocus
                        down = emailFocus
                    },
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    onImeActionPerformed = { _, _ ->
                        emailFocus.requestFocus()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    modifier = Modifier.focusOrder(emailFocus) {
                        previous = usernameFocus
                        up = usernameFocus
                        next = passwordFocus
                        down = passwordFocus
                    },
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next
                    ),
                    onImeActionPerformed = { _, _ ->
                        passwordFocus.requestFocus()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    modifier = Modifier.focusOrder(passwordFocus) {
                        previous = emailFocus
                        up = emailFocus
                        next = confirmFocus
                        down = confirmFocus
                    },
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    onImeActionPerformed = { _, _ ->
                        confirmFocus.requestFocus()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    modifier = Modifier.focusOrder(confirmFocus) {
                        previous = passwordFocus
                        up = passwordFocus
                    },
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("重复密码") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    onImeActionPerformed = { _, softwareKeyboardController ->
                        softwareKeyboardController?.hideSoftwareKeyboard()
                        confirmFocus.freeFocus()
                        onRegisterRequest(
                            username.text,
                            email.text,
                            password.text,
                            confirmPassword.text
                        )
                    }
                )
                StateFab(
                    modifier = Modifier.padding(top = 16.dp),
                    state = fabState, onClicked = {
                        onRegisterRequest(
                            username.text,
                            email.text,
                            password.text,
                            confirmPassword.text
                        )
                    }
                )
            }
        }
    }
}
