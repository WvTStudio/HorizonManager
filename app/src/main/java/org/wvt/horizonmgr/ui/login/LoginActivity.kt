package org.wvt.horizonmgr.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import org.wvt.horizonmgr.defaultViewModelFactory
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

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
            LoginActivity.LOGIN_SUCCEED -> {
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
            LoginActivity.LOGIN_CANCELED -> return LoginResult.Canceled
            else -> return LoginResult.Canceled
        }
    }
}

class LoginActivity : AppCompatActivity() {
    companion object {
        const val LOGIN_CANCELED = 0
        const val LOGIN_SUCCEED = 1

        const val EXTRA_UID = "uid"
        const val EXTRA_NAME = "name"
        const val EXTRA_ACCOUNT = "account"
        const val EXTRA_AVATAR = "avatar"
    }

    private val viewModel by viewModels<LoginViewModel> { defaultViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContent {
            AndroidHorizonManagerTheme {
                Surface {
                    Login(viewModel, ::onLoginSucceed, ::onCancel)
                }
            }
        }
    }

    private fun onCancel() {
        setResult(LOGIN_CANCELED)
        Log.d(TAG, "Login canceled.")
        finish()
    }

    private fun onLoginSucceed(account: String, avatar: String?, name: String, uid: String) {
        val result = Intent().apply {
            putExtra(EXTRA_UID, uid)
            putExtra(EXTRA_NAME, name)
            putExtra(EXTRA_ACCOUNT, account)
            putExtra(EXTRA_AVATAR, avatar)
        }
        setResult(LOGIN_SUCCEED, result)
        Log.d(TAG, "Login succeed: $result")
        finish()
    }
}