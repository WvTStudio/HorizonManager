package org.wvt.horizonmgr.ui.main

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import org.wvt.horizonmgr.ui.article.ArticleContent
import org.wvt.horizonmgr.ui.article.ArticleContentViewModel

@Composable
fun RootWithNavHost() {
    val navController = rememberNavController()
    NavHost(navController, "home") {
        composable("main") {

        }
        composable("file_chooser") {

        }
        composable("community") {

        }
        composable("group") {

        }
        composable("donate") {

        }
        composable("settings") {

        }
        composable("custom_theme") {

        }
        composable(
            "article_detail/{id}",
            listOf(navArgument("id") { type = NavType.IntType })
        ) {
            val articleDetailViewModel: ArticleContentViewModel = hiltViewModel()
            ArticleContent(vm = articleDetailViewModel, onNavClick = {
                navController.popBackStack()
            })
        }
        composable(
            "package_detail/{uuid}",
            listOf(navArgument("uuid") { type = NavType.StringType })
        ) {

        }
    }
}

@Composable
fun MainWithNavHost() {
    val navController = rememberNavController()
    NavHost(navController, "home") {
        composable("home") {

        }
        composable("package_manager") {

        }
        composable("mod_manager") {

        }
        composable("online_resource") {

        }
        composable("local_resource") {

        }
    }
}