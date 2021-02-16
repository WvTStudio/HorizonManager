package org.wvt.horizonmgr.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun RegisterPage(
    fabState: FabState,
    onRegisterRequest: (username: String, email: String, password: String, confirmPassword: String) -> Unit
) {
    val (usernameFocus, emailFocus, passwordFocus, confirmFocus) = FocusRequester.createRefs()
    var username by remember { mutableStateOf(TextFieldValue()) }
    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue()) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .wrapContentWidth()
                .fillMaxHeight()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Text("注册", color = MaterialTheme.colors.primary, fontSize = 64.sp)
                Providers(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        modifier = Modifier.padding(top = 16.dp),
                        text = "注册到 InnerCore 中文社区 ",
                    )
                }
                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
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
                        keyboardActions = KeyboardActions(onNext = {
                            emailFocus.requestFocus()
                        })

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
                        keyboardActions = KeyboardActions(onNext = {
                            passwordFocus.requestFocus()
                        })
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
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { confirmFocus.requestFocus() })
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
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            confirmFocus.freeFocus()
                            onRegisterRequest(
                                username.text,
                                email.text,
                                password.text,
                                confirmPassword.text
                            )
                        })
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
}
