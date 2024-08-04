import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import java.io.File

val resource by lazy {
  val file =
      File(System.getProperty("compose.application.resources.dir", ".")).resolve("resource.txt")
  if (file.exists()) file.readText() else "App"
}

fun main() = application {
  Window(
      title = resource,
      state =
          rememberWindowState(
              width = 800.dp, height = 600.dp, position = WindowPosition(Alignment.Center)),
      onCloseRequest = ::exitApplication) {
        window.minimumSize = Dimension(350, 600)
        App()
      }
}
