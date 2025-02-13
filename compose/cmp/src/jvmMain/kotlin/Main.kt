@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.Dimension
import java.io.File
import kotlin.jvm.optionals.getOrElse
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import ui.crash.windowExceptionHandlerFactory

val resource by lazy {
  val file =
      File(System.getProperty("compose.application.resources.dir", ".")).resolve("resource.txt")
  if (file.exists()) file.readText() else "App"
}

fun main() = application {
  CompositionLocalProvider(
      LocalWindowExceptionHandlerFactory provides windowExceptionHandlerFactory,
  ) {
    val windowState =
        rememberWindowState(
            width = 800.dp, height = 600.dp, position = WindowPosition(Alignment.Center))

    Window(title = resource, state = windowState, onCloseRequest = ::exitApplication) {
      window.minimumSize = Dimension(350, 600)
      App()
      showStartupTime()
    }
  }
}

fun showStartupTime() {
  val currTime = System.currentTimeMillis()
  val vmTime =
      ProcessHandle.current()
          .info()
          .startInstant()
          .getOrElse { Clock.System.now().toJavaInstant() }
          .toEpochMilli()
  println("Application started in ${currTime - vmTime} ms")
}
