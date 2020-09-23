package org.wvt.horizonmgr.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.compose.ui.platform.setContent
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.ui.AndroidDependenciesProvider
import org.wvt.horizonmgr.ui.saveUserInfo
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.ui.theme.HorizonManagerTheme

class LoginActivity : AppCompatActivity() {
    companion object {
        const val LOGIN_CANCEL = 0
        const val LOGIN_SUCCESS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidDependenciesProvider {
                AndroidHorizonManagerTheme {
                    Surface {
                        Login(::onLoginSuccess, ::onCancel)
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
        saveUserInfo(userInfo) // TODO use intent result
        setResult(LOGIN_SUCCESS)
        finish()
    }
}