package org.wvt.horizonmgr.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.wvt.horizonmgr.BuildConfig
import org.wvt.horizonmgr.ui.article.ArticleContentActivity
import org.wvt.horizonmgr.ui.article.ArticleContentScreen
import org.wvt.horizonmgr.ui.article.ArticleContentViewModel
import org.wvt.horizonmgr.ui.community.CommunityActivity
import org.wvt.horizonmgr.ui.community.CommunityScreen
import org.wvt.horizonmgr.ui.community.CommunityViewModel
import org.wvt.horizonmgr.ui.donate.DonateActivity
import org.wvt.horizonmgr.ui.donate.DonateScreen
import org.wvt.horizonmgr.ui.fileselector.FileSelector
import org.wvt.horizonmgr.ui.fileselector.FileSelectorActivity
import org.wvt.horizonmgr.ui.fileselector.SharedFileChooserViewModel
import org.wvt.horizonmgr.ui.joingroup.JoinGroupActivity
import org.wvt.horizonmgr.ui.joingroup.JoinGroupScreen
import org.wvt.horizonmgr.ui.login.LoginActivity
import org.wvt.horizonmgr.ui.login.LoginScreen
import org.wvt.horizonmgr.ui.main.MainScreen
import org.wvt.horizonmgr.ui.onlineinstall.InstallPackageActivity
import org.wvt.horizonmgr.ui.onlineinstall.OnlineInstallScreen
import org.wvt.horizonmgr.ui.pacakgemanager.PackageDetailActivity
import org.wvt.horizonmgr.ui.pacakgemanager.PackageDetailScreen
import org.wvt.horizonmgr.ui.settings.CustomThemeActivity
import org.wvt.horizonmgr.ui.settings.CustomThemeScreen
import org.wvt.horizonmgr.ui.settings.SettingsActivity
import org.wvt.horizonmgr.ui.settings.SettingsScreen
import org.wvt.horizonmgr.ui.theme.AndroidDonateTheme
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme

@Composable
fun RootNavHost(
    requestOpenGame: () -> Unit,
    requestOpenURL: (String) -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            val sharedFileChooserViewModel = SharedFileChooserViewModel

            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    MainScreen(
                        navigateToLogin = { navController.navigate("login") },
                        navigateToCommunity = { navController.navigate("community") },
                        navigateToJoinGroup = { navController.navigate("join_group") },
                        navigateToDonate = { navController.navigate("donate") },
                        navigateToSettings = { navController.navigate("settings") },
                        navigateToOnlineInstall = { navController.navigate("online_install") },
                        navigateToPackageDetail = { navController.navigate("package_detail/${it}") },
                        navigateToArticleDetail = { navController.navigate("article_detail/${it}") },
                        onAddPackageClicked = {
                            sharedFileChooserViewModel.setRequestCode("add_package")
                            navController.navigate("file_chooser")
                        },
                        onAddICTextureClick = {
                            sharedFileChooserViewModel.setRequestCode("ic_texture")
                            navController.navigate("file_chooser")
                        },
                        onAddICLevelClick = {
                            sharedFileChooserViewModel.setRequestCode("ic_level")
                            navController.navigate("file_chooser")
                        },
                        onAddMCLevelClick = {
                            sharedFileChooserViewModel.setRequestCode("mc_level")
                            navController.navigate("file_chooser")
                        },
                        onAddMCTextureClick = {
                            sharedFileChooserViewModel.setRequestCode("mc_texture")
                            navController.navigate("file_chooser")
                        },
                        onAddModClicked = {
                            sharedFileChooserViewModel.setRequestCode("add_mod")
                            navController.navigate("file_chooser")
                        },
                        requestOpenGame = requestOpenGame,
                    )
                }
            }
        }
        composable("login") {
            AndroidHorizonManagerTheme(true) {
                Surface(color = MaterialTheme.colors.background) {
                    LoginScreen(
                        viewModel = hiltViewModel(),
                        onLoginSuccess = { account, avatar, name, uid ->
                            navController.navigateUp()
                        },
                        onCancel = { navController.navigateUp() }
                    )
                }
            }
        }
        composable("join_group") {
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    JoinGroupScreen(
                        viewModel = hiltViewModel(),
                        onClose = { navController.popBackStack() },
                        openURL = requestOpenURL
                    )
                }
            }
        }
        composable("donate") {
            AndroidDonateTheme {
                DonateScreen(onClose = { navController.popBackStack() })
            }
        }
        composable("community") {
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val context = LocalContext.current
                    CommunityScreen(vm = remember(context) { CommunityViewModel { context } }) {
                        navController.popBackStack()
                    }
                }
            }
        }
        composable("settings") {
            AndroidHorizonManagerTheme(true) {
                Surface(color = MaterialTheme.colors.background) {
                    SettingsScreen(
                        versionName = remember { "Version " + BuildConfig.VERSION_NAME },
                        onBackClick = { navController.popBackStack() },
                        requestCustomTheme = {
                            navController.navigate("custom_theme")
                        }
                    )
                }
            }
        }
        composable("custom_theme") {
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    CustomThemeScreen { navController.popBackStack() }
                }
            }
        }
        composable("online_install") {
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    OnlineInstallScreen(
                        viewModel = hiltViewModel(),
                        onCancel = {
                            // TODO: 2021/5/21
                            navController.navigateUp()
                        },
                        onSucceed = {
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
        composable(
            "package_detail/{uuid}",
            listOf(navArgument("uuid") { type = NavType.StringType })
        ) {
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    PackageDetailScreen(viewModel = hiltViewModel()) {
                        navController.popBackStack()
                    }
                }
            }
        }
        composable("article_detail/{id}", listOf(navArgument("id") { type = NavType.StringType })) {
            val articleDetailViewModel: ArticleContentViewModel = hiltViewModel()
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    ArticleContentScreen(
                        vm = articleDetailViewModel,
                        onNavClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
        composable("file_chooser") {
            val viewModel = SharedFileChooserViewModel
            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    FileSelector(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = hiltViewModel(),
                        onSelect = {
                            viewModel.setSelected(it)
                            navController.navigateUp()
                        },
                        onClose = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RootNavHostActivity(
    requestOpenGame: () -> Unit,
    requestOpenURL: (String) -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            val sharedFileChooserViewModel = SharedFileChooserViewModel

            AndroidHorizonManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    MainScreen(
                        navigateToLogin = { navController.navigate("login") },
                        navigateToCommunity = { navController.navigate("community") },
                        navigateToJoinGroup = { navController.navigate("join_group") },
                        navigateToDonate = { navController.navigate("donate") },
                        navigateToSettings = { navController.navigate("settings") },
                        navigateToOnlineInstall = { navController.navigate("online_install") },
                        navigateToPackageDetail = { navController.navigate("package_detail/${it}") },
                        navigateToArticleDetail = { navController.navigate("article_detail/${it}") },
                        onAddPackageClicked = {
                            sharedFileChooserViewModel.setRequestCode("add_package")
                            navController.navigate("file_chooser")
                        },
                        onAddICTextureClick = {
                            sharedFileChooserViewModel.setRequestCode("ic_texture")
                            navController.navigate("file_chooser")
                        },
                        onAddICLevelClick = {
                            sharedFileChooserViewModel.setRequestCode("ic_level")
                            navController.navigate("file_chooser")
                        },
                        onAddMCLevelClick = {
                            sharedFileChooserViewModel.setRequestCode("mc_level")
                            navController.navigate("file_chooser")
                        },
                        onAddMCTextureClick = {
                            sharedFileChooserViewModel.setRequestCode("mc_texture")
                            navController.navigate("file_chooser")
                        },
                        onAddModClicked = {
                            sharedFileChooserViewModel.setRequestCode("add_mod")
                            navController.navigate("file_chooser")
                        },
                        requestOpenGame = requestOpenGame,
                    )
                }
            }
        }
        activity("login") {
            activityClass = LoginActivity::class
        }
        activity("join_group") {
            activityClass = JoinGroupActivity::class
        }
        activity("donate") {
            activityClass = DonateActivity::class
        }
        activity("community") {
            activityClass = CommunityActivity::class
        }
        activity("settings") {
            activityClass = SettingsActivity::class
        }
        activity("custom_theme") {
            activityClass = CustomThemeActivity::class
        }
        activity("online_install") {
            activityClass = InstallPackageActivity::class
        }
        activity("package_detail/{uuid}") {
            activityClass = PackageDetailActivity::class
            argument("uuid") { type = NavType.StringType }
        }
        activity("article_detail/{id}") {
            argument("id") { type = NavType.StringType }
            activityClass = ArticleContentActivity::class
        }
        activity("file_chooser") {
            activityClass = FileSelectorActivity::class
        }
    }
}