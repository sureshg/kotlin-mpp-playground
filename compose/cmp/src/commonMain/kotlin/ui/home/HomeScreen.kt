package ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import dev.suresh.compose.res.*
import dev.suresh.platform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ui.debug
import ui.lottie.lottie

@Composable
@Preview
fun Home(navToFile: () -> Unit, navToImage: () -> Unit) {
  val coroutineScope = rememberCoroutineScope()
  var text by rememberSaveable { mutableStateOf(AnnotatedString("Compose Multiplatform!")) }
  var showImage by rememberSaveable { mutableStateOf(false) }

  Column(
      modifier =
          Modifier.fillMaxSize().padding(10.dp).debug(color = MaterialTheme.colorScheme.primary),
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
                append("Compose ${platform.name} App: ")
                withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                  append(BuildConfig.version)
                }
                appendLine()

                append("Java : ")
                withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                  append(BuildConfig.java)
                }
                appendLine()

                append("Kotlin : ")
                withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                  append(KotlinVersion.CURRENT.toString())
                }
                appendLine()

                append("Compose Multiplatform : ")
                withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                  append(BuildConfig.jetbrainsCompose)
                }
              }
              showImage = !showImage
            }) {
              Text(text = "Click")
            }

        AnimatedVisibility(visible = showImage) {
          Row {
            Image(
                modifier = Modifier.size(100.dp),
                imageVector = vectorResource(Res.drawable.compose_multiplatform),
                contentDescription = "Logo")

            lottie()
          }
        }

        Row {
          ElevatedButton(onClick = navToFile) {
            Icon(
                painter = painterResource(Res.drawable.ic_fluent_rocket),
                contentDescription = "File Browser",
                modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text("File Browser!")
          }

          Spacer(modifier = Modifier.width(5.dp))

          ElevatedButton(onClick = navToImage) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Bird Images",
                modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text("Bird Images!")
          }
        }
      }
}

@Composable
fun HomeButton(navToHome: () -> Unit) {
  ElevatedButton(onClick = navToHome) {
    Icon(
        imageVector = Icons.Default.Home,
        contentDescription = "Home",
        modifier = Modifier.size(24.dp))
    Spacer(modifier = Modifier.width(5.dp))
    Text("Back to Home")
  }
}
