import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
  var text by remember { mutableStateOf(AnnotatedString("Hello, Compose!")) }

  MaterialTheme {
    Column {
      Text(text)
      Button(
          onClick = {
            text = buildAnnotatedString {
              append("Compose Desktop App: ")
              withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                append(BuildConfig.version)
              }
              appendLine()

              append("JavaVersion: ")
              withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                append(BuildConfig.java)
              }
              appendLine()

              append("Kotlin Version: ")
              withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                append(KotlinVersion.CURRENT.toString())
              }
              appendLine()

              append("Compose Multiplatform Version: ")
              withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                append(BuildConfig.jetbrainsCompose)
              }
              appendLine()

              append("Compose Multiplatform Compiler Version: ")
              withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                append(BuildConfig.jetbrainsComposeCompiler)
              }
              appendLine()
            }
          }) {
            Text("Click")
          }
    }
  }
}

fun main() = application { Window(onCloseRequest = ::exitApplication) { App() } }
