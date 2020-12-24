package org.wvt.horizonmgr.ui.login

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.legacyservice.WebAPI
import org.wvt.horizonmgr.ui.components.FabState

class LoginViewModel(private val dependencies: DependenciesContainer) : ViewModel() {
    private val webApi = dependencies.webapi
    val fabState = MutableStateFlow<FabState>(FabState.TODO)

    @OptIn(ExperimentalMaterialApi::class)
    fun login(
        account: String,
        password: String,
        snackbarHostState: SnackbarHostState,
        onLoginSuccess: (WebAPI.UserInfo) -> Unit
    ) {
        // TODO: 2020/11/13 要不要解耦啊？我也不知道
        viewModelScope.launch {
            fabState.value = FabState.LOADING
            val userInfo = try {
                webApi.login(account, password)
            } catch (e: WebAPI.WebAPIException) {
                fabState.value = FabState.FAILED
                launch {
                    snackbarHostState.showSnackbar(e.message, "确定")
                    fabState.value = FabState.TODO
                }
                return@launch
            } catch (e: Exception) {
                e.printStackTrace()
                fabState.value = FabState.FAILED
                launch {
                    snackbarHostState.showSnackbar("未知错误，请稍后重试", "确定")
                    fabState.value = FabState.TODO
                }
                return@launch
            }
            fabState.value = FabState.SUCCEED
            launch { snackbarHostState.showSnackbar("登录成功") }
            delay(800)
            onLoginSuccess(userInfo)
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    fun register(
        username: String,
        email: String,
        pass: String,
        confirmPass: String,
        snackbarHostState: SnackbarHostState,
        onSucceed: (uid: String, username: String, password: String) -> Unit
    ) {
        viewModelScope.launch {
            fabState.value = FabState.LOADING

            if (pass != confirmPass) {
                fabState.value = FabState.FAILED
                snackbarHostState.showSnackbar("重复密码不一致", "确认")
                fabState.value = FabState.TODO
            } else {
                val uid = try {
                    webApi.register(username, email, pass)
                } catch (e: WebAPI.RegisterException) {
                    fabState.value = FabState.FAILED
                    snackbarHostState.showSnackbar(e.errors.first().detail, "确认")
                    fabState.value = FabState.TODO
                    return@launch
                } catch (e: WebAPI.NetworkException) {
                    fabState.value = FabState.FAILED
                    snackbarHostState.showSnackbar(e.message, "确认")
                    fabState.value = FabState.TODO
                    return@launch
                } catch (e: Exception) {
                    fabState.value = FabState.FAILED
                    snackbarHostState.showSnackbar("未知错误，请稍后重试", "确认")
                    fabState.value = FabState.TODO
                    return@launch
                }
                fabState.value = FabState.SUCCEED
                snackbarHostState.showSnackbar("注册成功，注意查收验证邮件", "确认")
                fabState.value = FabState.TODO
                onSucceed(uid, username, pass)
            }
        }
    }
}