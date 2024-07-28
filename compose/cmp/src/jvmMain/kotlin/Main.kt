import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ElevatedButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.suresh.compose.res.Res
import dev.suresh.compose.res.ic_fluent_rocket_24_filled
import dev.suresh.compose.res.idea_logo
import java.awt.Dimension
import java.io.File
import org.jetbrains.compose.resources.painterResource

val resourcesDir = File(System.getProperty("compose.application.resources.dir", "."))

@Composable
@Preview
fun Home(navController: NavController) {
  val coroutineScope = rememberCoroutineScope()
  var text by rememberSaveable { mutableStateOf(AnnotatedString("Hello, Compose!")) }
  var showImage by rememberSaveable { mutableStateOf(false) }
  val resource = resourcesDir.resolve("resource.txt")

  Column(
      modifier = Modifier.fillMaxSize().padding(10.dp).debug(color = Color.Blue),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceEvenly) {
        SelectionContainer {
          Text(
              text,
              modifier = Modifier.basicMarquee().fillMaxWidth().align(Alignment.CenterHorizontally))
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

                append("Resource: ")
                withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                  if (resource.exists()) append(resource.readText()) else append("Not found")
                }
              }
              showImage = !showImage
            }) {
              Text(text = "Click")
            }

        AnimatedVisibility(visible = showImage) {
          Image(painter = painterResource(Res.drawable.idea_logo), contentDescription = "Logo")
        }

        ElevatedButton(onClick = { navController.navigate("FileBrowser") }) {
          Icon(
              painter = painterResource(Res.drawable.ic_fluent_rocket_24_filled),
              contentDescription = "File Browser",
              modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(5.dp))
          Text("File Browser!")
        }
      }
}

@Composable
fun FileBrowser(modifier: Modifier = Modifier, navController: NavController) {
  Column(
      modifier = modifier.fillMaxSize().padding(10.dp).debug(color = Color.Magenta),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceEvenly) {
        DragDropListView()
        Button(onClick = { navController.popBackStack() }) { Text("Back") }
      }
}

@Composable
fun App() {
  SaveToBitmap(
      onSave = {
        // val img = it.toAwtImage()
        // val file = File("screenshot.png")
        // ImageIO.write(img, "png", file)
        // println("Saved the screenshot to ${file.absolutePath}")
      }) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "Home") {
          composable("Home") { Home(navController) }
          composable("FileBrowser") { FileBrowser(navController = navController) }
        }
      }
}

fun main() = application {
  Window(
      title = "App",
      state =
          rememberWindowState(
              width = 800.dp, height = 600.dp, position = WindowPosition(Alignment.Center)),
      onCloseRequest = ::exitApplication) {
        window.minimumSize = Dimension(350, 600)
        MaterialTheme { App() }
      }
}
