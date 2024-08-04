package nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ui.birds.BirdImages
import ui.file.FileBrowser
import ui.home.Home

@Composable
fun NavRoot() {
  val navController = rememberNavController()
  NavHost(navController = navController, startDestination = Screen.Home.route) {
    composable(Screen.Home.route) {
      Home(
          navToFile = { navController.navigate(Screen.FileBrowser.route) },
          navToImage = { navController.navigate(Screen.Image.route) })
    }
    composable(Screen.FileBrowser.route) {
      FileBrowser(navToHome = { navController.popBackStack() })
    }

    composable(Screen.Image.route) { BirdImages(navToHome = { navController.popBackStack() }) }

    composable(
        route = Screen.Profile.route,
        arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
          // val userId = it.arguments?.getString("userId")
          // Profile(userId = userId)
        }
  }
}
