package org.wvt.horizonmgr.ui.login

import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.ui.components.FabState
import org.wvt.horizonmgr.utils.LocalCache
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.iccn.ICCNModule
import javax.inject.Inject

private const val TAG = "LoginViewModelLogger"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val localCache: LocalCache,
    private val iccn: ICCNModule
) : ViewModel() {
    val fabState = MutableStateFlow<FabState>(FabState.TODO)

    fun login(
        account: String,
        password: String,
        snackbarHostState: SnackbarHostState,
        onLoginSuccess: (account: String, avatar: String?, name: String, uid: String) -> Unit
    ) {
        viewModelScope.launch {
            fabState.emit(FabState.LOADING)
            val userInfo = try {
                withContext(Dispatchers.IO) { iccn.login(account, password) }
            } catch (e: ICCNModule.LoginFailedException) {
                fabState.emit(FabState.FAILED)
                snackbarHostState.showSnackbar("账号或密码错误", "确定")
                fabState.emit(FabState.TODO)
                return@launch
            } catch (e: NetworkException) {
                Log.d(TAG, "登录失败，网络错误", e)
                fabState.emit(FabState.FAILED)
                snackbarHostState.showSnackbar("网络错误，请稍后重试", "确定")
                fabState.emit(FabState.TODO)
                return@launch
            } catch (e: Exception) {
                Log.d(TAG, "注册失败，未知错误", e)
                fabState.emit(FabState.FAILED)
                snackbarHostState.showSnackbar("未知错误，请稍后重试", "确定")
                fabState.emit(FabState.TODO)
                return@launch
            }
            fabState.emit(FabState.SUCCEED)
            localCache.cacheUserInfo(
                LocalCache.CachedUserInfo(
                    userInfo.uid,
                    userInfo.name,
                    userInfo.account,
                    userInfo.avatarUrl!!
                )
            )
            launch { snackbarHostState.showSnackbar("登录成功") } // 此处用 launch 的原因是为了 UX，只希望等待 800ms
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
            fabState.emit(FabState.LOADING)

            if (pass != confirmPass) {
                fabState.emit(FabState.FAILED)
                snackbarHostState.showSnackbar("重复密码不一致", "确认")
                fabState.emit(FabState.TODO)
            } else {
                val uid = try {
                    withContext(Dispatchers.IO) { iccn.register(username, email, pass) }
                } catch (e: ICCNModule.RegisterFailedException) {
                    fabState.emit(FabState.FAILED)
                    snackbarHostState.showSnackbar(e.errors.first().detail, "确认")
                    fabState.emit(FabState.TODO)
                    return@launch
                } catch (e: NetworkException) {
                    Log.e(TAG, "注册失败，网络错误", e)
                    fabState.emit(FabState.FAILED)
                    snackbarHostState.showSnackbar("网络错误，请稍后重试", "确认")
                    fabState.emit(FabState.TODO)
                    return@launch
                } catch (e: Exception) {
                    Log.d(TAG, "注册失败，未知错误", e)
                    fabState.emit(FabState.FAILED)
                    snackbarHostState.showSnackbar("未知错误，请稍后重试", "确认")
                    fabState.emit(FabState.TODO)
                    return@launch
                }
                fabState.emit(FabState.SUCCEED)
                snackbarHostState.showSnackbar("注册成功，注意查收验证邮件", "确认")
                fabState.emit(FabState.TODO)
                onSucceed(uid, username, pass)
            }
        }
    }
}