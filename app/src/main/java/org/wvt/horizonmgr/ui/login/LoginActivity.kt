package org.wvt.horizonmgr.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.compose.ui.platform.setContent
import kotlinx.coroutines.suspendCancellableCoroutine
import org.wvt.horizonmgr.legacyservice.LocalCache
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import kotlin.coroutines.resume

private const val TAG = "LoginActivity"

sealed class LoginResult {
    data class Succeed(
        val uid: String,
        val name: String,
        val account: String,
        val avatar: String
    ) : LoginResult()

    object Canceled : LoginResult()
}


class LoginResultContract : ActivityResultContract<Context, LoginResult>() {
    override fun createIntent(context: Context, input: Context): Intent {
        return Intent(input, LoginActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): LoginResult {
        when (resultCode) {
            LoginActivity.LOGIN_SUCCESS -> {
                if (intent == null) return LoginResult.Canceled
                return with(intent) {
                    LoginResult.Succeed(
                        getStringExtra(LoginActivity.EXTRA_UID)
                            ?: error("The key 'uid' was not specified."),
                        getStringExtra(LoginActivity.EXTRA_NAME)
                            ?: error("The key 'name' was not specified."),
                        getStringExtra(LoginActivity.EXTRA_ACCOUNT)
                            ?: error("The key 'account' was not specified."),
                        getStringExtra(LoginActivity.EXTRA_AVATAR)
                            ?: error("The key 'uid' avatar not specified.")
                    )
                }
            }
            LoginActivity.LOGIN_CANCEL -> return LoginResult.Canceled
            else -> return LoginResult.Canceled
        }
    }
}

suspend fun LoginActivity.Companion.startForResult(activity: AppCompatActivity): LoginResult {
    return suspendCancellableCoroutine<LoginResult> { cont ->
        activity.registerForActivityResult(LoginResultContract(), activity.activityResultRegistry) {
            cont.resume(it)
        }.launch(activity)
    }
}

class LoginActivity : AppCompatActivity() {
    companion object {
        const val LOGIN_CANCEL = 0
        const val LOGIN_SUCCESS = 1

        const val EXTRA_UID = "uid"
        const val EXTRA_NAME = "name"
        const val EXTRA_ACCOUNT = "account"
        const val EXTRA_AVATAR = "avatar"

        fun Intent.getResult(): LocalCache.CachedUserInfo? {
            return LocalCache.CachedUserInfo(
                getStringExtra(EXTRA_UID) ?: return null,
                getStringExtra(EXTRA_NAME) ?: return null,
                getStringExtra(EXTRA_ACCOUNT) ?: return null,
                getStringExtra(EXTRA_AVATAR) ?: return null
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidHorizonManagerTheme {
                Surface {
                    Login(::onLoginSuccess, ::onCancel)
                }
            }
        }
    }

    private fun onCancel() {
        setResult(LOGIN_CANCEL)
        finish()
    }

    private fun onLoginSuccess(account: String, avatar: String?, name: String, uid: String) {
        val result = Intent().apply {
            putExtra(EXTRA_UID, uid)
            putExtra(EXTRA_NAME, name)
            putExtra(EXTRA_ACCOUNT, account)
            putExtra(EXTRA_AVATAR, avatar)
        }
        setResult(LOGIN_SUCCESS, result)
        Log.d(TAG, "Login succeed: $result")
        finish()
    }
}