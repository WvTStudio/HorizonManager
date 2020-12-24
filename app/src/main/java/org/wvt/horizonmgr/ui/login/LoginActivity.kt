package org.wvt.horizonmgr.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.compose.ui.platform.setContent
import org.wvt.horizonmgr.dependenciesViewModel
import org.wvt.horizonmgr.legacyservice.LocalCache
import org.wvt.horizonmgr.legacyservice.WebAPI
import org.wvt.horizonmgr.ui.AndroidDependenciesProvider
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

class LoginActivity : AppCompatActivity() {
    companion object {
        const val LOGIN_CANCEL = 0
        const val LOGIN_SUCCESS = 1

        fun Intent.getResult(): LocalCache.CachedUserInfo? {
            return LocalCache.CachedUserInfo(
                getIntExtra("id", -1).also { if (it == -1) return null },
                getStringExtra("name") ?: return null,
                getStringExtra("account") ?: return null,
                getStringExtra("avatarUrl") ?: return null
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm = dependenciesViewModel<LoginViewModel>()

            AndroidDependenciesProvider {
                AndroidHorizonManagerTheme {
                    Surface {
                        Login(vm, ::onLoginSuccess, ::onCancel)
                    }
                }
            }
        }
    }

    private fun onCancel() {
        setResult(LOGIN_CANCEL)
        finish()
    }

    private fun onLoginSuccess(userInfo: WebAPI.UserInfo) {
        setResult(LOGIN_SUCCESS,
            Intent().apply {
                putExtra("id", userInfo.id)
                putExtra("name", userInfo.name)
                putExtra("account", userInfo.account)
                putExtra("avatarUrl", userInfo.avatarUrl)
            }
        )
        finish()
    }
}