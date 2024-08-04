package nav

sealed class Screen(val route: String) {
  data object Home : Screen("home")

  data object FileBrowser : Screen("fileBrowser")

  data object Image : Screen("image")

  data object Video : Screen("video")

  data object Profile : Screen("profile?userId={userId}")
}
