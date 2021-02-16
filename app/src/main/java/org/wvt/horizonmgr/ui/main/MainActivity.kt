package org.wvt.horizonmgr.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.tooling.preview.Preview
import org.wvt.horizonmgr.HorizonManagerApplication
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.community.CommunityActivity
import org.wvt.horizonmgr.ui.components.MyAlertDialog
import org.wvt.horizonmgr.ui.donate.DonateActivity
import org.wvt.horizonmgr.ui.joingroup.JoinGroupActivity
import org.wvt.horizonmgr.ui.login.LoginResultContract
import org.wvt.horizonmgr.ui.settings.SettingsActivity
import org.wvt.horizonmgr.ui.startActivity
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.ui.theme.PreviewTheme

class MainActivity : AppCompatActivity() {

    private lateinit var app: HorizonManagerApplication
    private lateinit var vm: MainActivityViewModel

    private val login = registerForActivityResult(LoginResultContract()) {
        vm.setUserInfo(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_HorizonManagerCompose_NoActionBar) // cancel the slash theme
        app = application as HorizonManagerApplication

        vm =
            viewModels<MainActivityViewModel>(factoryProducer = { app.dependenciesVMFactory }).value

        setContent {
//            val vm = dependenciesViewModel<MainActivityViewModel>()
            val initializing by vm.initializing.collectAsState()
            val userInfo by vm.userInfo.collectAsState()
            val selectedPackage by vm.selectedPackage.collectAsState()
            val showPermissionDialog by vm.showPermissionDialog.collectAsState()

            val newVersion by vm.newVersion.collectAsState()
            var displayNewVersionDialog by rememberSaveable { mutableStateOf(false) }

            DisposableEffect(newVersion) {
                if (newVersion != null) {
                    displayNewVersionDialog = true
                }
                onDispose {}
            }

            DisposableEffect(Unit) {
                vm.checkPermission(this@MainActivity)
                vm.getUpdate()
                onDispose {
                    // TODO: 2021/2/6 添加 Cancel 逻辑
                }
            }

            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    if (!initializing) App(
                        userInfo = userInfo,
                        requestLogin = { login.launch(this) },
                        requestLogout = vm::logOut,
                        selectedPackageUUID = selectedPackage,
                        selectedPackageChange = vm::setSelectedPackage,
                        openGame = { vm.openGame(this) },
                        community = { startActivity<CommunityActivity>() },
                        joinGroup = { startActivity<JoinGroupActivity>() },
                        donate = { startActivity<DonateActivity>() },
                        settings = { startActivity<SettingsActivity>() },
                    )
                }

                if (showPermissionDialog) {
                    RequestPermissionDialog {
                        vm.dismiss()
                        vm.requestPermission(this)
                    }
                }

                val theNewVersion = newVersion

                if (theNewVersion != null && displayNewVersionDialog) {
                    NewVersionDialog(
                        versionName = theNewVersion.versionName,
                        versionCode = theNewVersion.versionCode,
                        changelog = theNewVersion.changelog,
                        onConfirm = { displayNewVersionDialog = false },
                        onIgnore = {
                            vm.ignoreVersion(theNewVersion.versionCode)
                            displayNewVersionDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestPermissionDialog(
    onConfirm: () -> Unit
) {
    MyAlertDialog(
        onDismissRequest = { },
        confirmButton = { TextButton(onClick = onConfirm) { Text(text = "授权") } },
        title = { Text("需要权限") },
        text = { Text("本应用需要拥有网络权限及对内置存储的完全访问权限。\n如果您的系统版本为 Android R 及以上，您需要在弹出的系统设置中授予完全访问权限") }
    )
}

@Composable
private fun NewVersionDialog(
    versionName: String,
    versionCode: Int,
    changelog: String,
    onConfirm: () -> Unit,
    onIgnore: () -> Unit
) {
    MyAlertDialog(
        title = { Text("发现新版本") },
        text = {
            LazyColumn(Modifier.fillMaxHeight(0.6f)) {
                item {
                    Text(
                        """
                        |版本名：${versionName}
                        |版本号：${versionCode}
                        |更新日志：
                        |${changelog}
                        """.trimMargin()
                    )
                }
            }
        },
        onDismissRequest = onConfirm,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onIgnore) {
                Text("忽略该版本")
            }
        }
    )
}

@Preview
@Composable
private fun NewVersionDialogPreview() {
    PreviewTheme {
        Surface(Modifier.fillMaxSize()) {
            NewVersionDialog(
                versionName = "2.0.0",
                versionCode = 100,
                changelog = """
                    社区界面
                    - 将社区界面更换为 Jetpack Compose 方案
                    - 添加 “开始新下载任务” 的对话框

                    自定义主题
                    - 改善切换夜间模式的代码逻辑
                    - 更改主题颜色时可以平滑过渡

                    加入群组界面
                    - 网络出错时会显示错误而不是崩溃
                    - QQ 无法打开时会显示错误而不是崩溃

                    在线资源界面
                    - 现在在未登录的状态下将不显示过滤按钮

                    分包管理界面
                    - 现在安装一个分包后，数据会自动刷新
                    - 分包详情入口移至菜单
                    - 分包详情将显示更多信息
                    - 现在点击本地安装会打开文件选择页面，但功能未实现

                    文件选择界面
                    - 改进收藏文件夹的动画效果

                    对话框界面
                    - 进度对话框现在正确遵守 Material Design
                    - 对话框现在以 24dp 的海拔显示
                    - InputDialog 现在以 24dp 海拔显示

                    模组管理界面
                    - 使所有 Tab 的逻辑保持一致性
                    - 在未选择分包的时候不会再显示安装按钮，且安装按钮只在 Mod Tab 显示

                    其他
                    - 改善错误提示的显示效果
                    - 修复部分 Bug
                """.trimIndent(),
                onConfirm = {},
                onIgnore = {}
            )
        }
    }
}