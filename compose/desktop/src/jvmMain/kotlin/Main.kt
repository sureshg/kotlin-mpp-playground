import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
  var text by remember { mutableStateOf("Hello, Compose!") }

  MaterialTheme {
    Column {
      Text(text)
      Button(
          onClick = {
            text =
                """
                | Hello, Desktop! ${BuildConfig.version}
                | App Version: ${BuildConfig.version}
                | JavaVersion: ${BuildConfig.java}
                | Kotlin Version: ${KotlinVersion.CURRENT}
                | Compose Multiplatform Version: ${BuildConfig.jetbrainsCompose}
                | Compose Multiplatform Compiler Version: ${BuildConfig.jetbrainsComposeCompiler}
                """
                    .trimMargin()
          }) {
            Text("Click")
          }
    }
  }
}

fun main() = application { Window(onCloseRequest = ::exitApplication) { App() } }
