package org.wvt.horizonmgr.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import org.wvt.horizonmgr.BuildConfig
import org.wvt.horizonmgr.ui.article.ArticleContentScreen
import org.wvt.horizonmgr.ui.article.ArticleContentViewModel
import org.wvt.horizonmgr.ui.community.CommunityScreen
import org.wvt.horizonmgr.ui.community.CommunityViewModel
import org.wvt.horizonmgr.ui.donate.DonateScreen
import org.wvt.horizonmgr.ui.fileselector.FileSelector
import org.wvt.horizonmgr.ui.fileselector.SharedFileChooserViewModel
import org.wvt.horizonmgr.ui.joingroup.JoinGroupScreen
import org.wvt.horizonmgr.ui.login.LoginScreen
import org.wvt.horizonmgr.ui.main.MainScreen
import org.wvt.horizonmgr.ui.onlineinstall.OnlineInstallScreen
import org.wvt.horizonmgr.ui.pacakgemanager.PackageDetailScreen
import org.wvt.horizonmgr.ui.settings.CustomThemeScreen
import org.wvt.horizonmgr.ui.settings.SettingsScreen

@Composable
fun RootNavHost(
    requestOpenGame: () -> Unit,
    requestOpenURL: (String) -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            val sharedFileChooserViewModel = hiltViewModel<SharedFileChooserViewModel>()

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
        composable("login") {
            LoginScreen(
                viewModel = hiltViewModel(),
                onLoginSuccess = { account, avatar, name, uid ->
                    navController.navigateUp()
                },
                onCancel = { navController.navigateUp() }
            )
        }
        composable("join_group") {
            JoinGroupScreen(
                viewModel = hiltViewModel(),
                onClose = { navController.popBackStack() },
                openURL = requestOpenURL
            )
        }
        composable("donate") {
            DonateScreen(onClose = { navController.popBackStack() })
        }
        composable("community") {
            val context = LocalContext.current
            CommunityScreen(vm = remember(context) { CommunityViewModel { context } }) {
                navController.popBackStack()
            }
        }
        composable("settings") {
            SettingsScreen(
                versionName = remember { "Version" + BuildConfig.VERSION_NAME },
                onBackClick = { navController.popBackStack() },
                requestCustomTheme = {
                    navController.navigate("custom_theme")
                }
            )
        }
        composable("custom_theme") {
            CustomThemeScreen { navController.popBackStack() }
        }
        composable("online_install") {
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
        composable(
            "package_detail/{uuid}",
            listOf(navArgument("uuid") { type = NavType.StringType })
        ) {
            PackageDetailScreen(viewModel = hiltViewModel()) {
                navController.popBackStack()
            }
        }
        composable("article_detail/{id}", listOf(navArgument("id") { type = NavType.StringType })) {
            val articleDetailViewModel: ArticleContentViewModel = hiltViewModel()
            ArticleContentScreen(
                vm = articleDetailViewModel,
                onNavClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("file_chooser") {
            val viewModel = viewModel<SharedFileChooserViewModel>()
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