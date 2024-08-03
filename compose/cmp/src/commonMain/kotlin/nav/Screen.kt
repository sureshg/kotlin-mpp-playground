package nav

sealed class Screen(val route: String) {
  data object Home : Screen("home")

  data object FileBrowser : Screen("fileBrowser")
}
