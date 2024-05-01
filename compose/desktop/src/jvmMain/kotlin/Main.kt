import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File

val debug = true

val resourcesDir = File(System.getProperty("compose.application.resources.dir", "."))

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun App() {
  var text by remember { mutableStateOf(AnnotatedString("Hello, Compose!")) }
  var showImage by remember { mutableStateOf(false) }
  scrollingBox {
    Column(
        modifier = Modifier.fillMaxWidth().debug(color = Color.Blue),
        horizontalAlignment = Alignment.CenterHorizontally) {
          SelectionContainer {
            Text(
                text,
                modifier =
                    Modifier.basicMarquee().fillMaxWidth().align(Alignment.CenterHorizontally))
          }

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
                }
                showImage = !showImage
              }) {
                Text(text = "Click")
              }

          AnimatedVisibility(visible = showImage) {
            Image(
                painter = painterResource("svg/idea-logo.svg"),
                contentDescription = "Logo",
                modifier = Modifier.fillMaxSize())
          }
        }
  }
}

fun main() = application {
  Window(title = "App", onCloseRequest = ::exitApplication) { MaterialTheme { App() } }
}
