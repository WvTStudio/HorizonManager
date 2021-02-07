package org.wvt.horizonmgr.ui.login

import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.ui.components.FabState
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.iccn.ICCNModule

private const val TAG = "LoginViewModelLogger"

class LoginViewModel(private val dependencies: DependenciesContainer) : ViewModel() {
    private val iccn = dependencies.iccn
    val fabState = MutableStateFlow<FabState>(FabState.TODO)

    fun login(
        account: String,
        password: String,
        snackbarHostState: SnackbarHostState,
        onLoginSuccess: (account: String, avatar: String?, name: String, uid: String) -> Unit
    ) {
        viewModelScope.launch {
            fabState.value = FabState.LOADING
            val userInfo = try {
                iccn.login(account, password)
            } catch (e: ICCNModule.LoginFailedException) {
                fabState.value = FabState.FAILED
                launch {
                    snackbarHostState.showSnackbar("账号或密码错误", "确定")
                    fabState.value = FabState.TODO
                }
                return@launch
            } catch (e: NetworkException) {
                Log.d(TAG, "登录失败，网络错误", e)
                fabState.value = FabState.FAILED
                launch {
                    snackbarHostState.showSnackbar("网络错误，请稍后重试", "确定")
                    fabState.value = FabState.TODO
                }
                return@launch
            } catch (e: Exception) {
                Log.d(TAG, "注册失败，未知错误", e)
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
            onLoginSuccess(userInfo.account, userInfo.avatarUrl, userInfo.name, userInfo.uid)
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
                    iccn.register(username, email, pass)
                } catch (e: ICCNModule.RegisterFailedException) {
                    fabState.value = FabState.FAILED
                    snackbarHostState.showSnackbar(e.errors.first().detail, "确认")
                    fabState.value = FabState.TODO
                    return@launch
                } catch (e: NetworkException) {
                    Log.e(TAG, "注册失败，网络错误", e)
                    fabState.value = FabState.FAILED
                    snackbarHostState.showSnackbar("网络错误，请稍后重试", "确认")
                    fabState.value = FabState.TODO
                    return@launch
                } catch (e: Exception) {
                    Log.d(TAG, "注册失败，未知错误", e)
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