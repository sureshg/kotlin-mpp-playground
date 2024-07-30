import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.suresh.Greeting
import dev.suresh.compose.res.*
import kotlinx.coroutines.launch
import model.BirdsViewModel
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun CommonApp() {
  AppTheme {
    var show by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
      Button(onClick = { show = !show }) { Text("Click me!") }
      AnimatedVisibility(show) {
        val greeting = remember { Greeting().greeting() }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
          Image(vectorResource(Res.drawable.compose_logo), "Compose Logo!")
          Text("Compose: $greeting")
        }
      }
    }
  }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
  MaterialTheme(
      colorScheme = MaterialTheme.colorScheme.copy(primary = Color.Black),
      shapes =
          MaterialTheme.shapes.copy(
              small = RoundedCornerShape(0.dp),
              medium = RoundedCornerShape(0.dp),
              large = RoundedCornerShape(0.dp))) {
        content()
      }
}

@Composable
fun BirdsImages(modifier: Modifier = Modifier) {
  val birdsViewModel = viewModel { BirdsViewModel() }
  val state by birdsViewModel.uiState.collectAsState()

  // Load the BirdUiState
  LaunchedEffect(birdsViewModel) { birdsViewModel.update() }

  Column(modifier = modifier.verticalScroll(rememberScrollState())) {
    state.images.forEach { image ->
      Text(image.path)
      Spacer(modifier = Modifier.width(5.dp))
    }
  }
}

@Composable
fun SaveToBitmap(
    modifier: Modifier = Modifier,
    onSave: (ImageBitmap) -> Unit,
    content: @Composable () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  val graphicsLayer = rememberGraphicsLayer()
  Box(
      modifier =
          modifier
              .drawWithContent {
                graphicsLayer.record {
                  // Draw the content of the composable to the graphics layer.
                  this@drawWithContent.drawContent()
                }
                drawLayer(graphicsLayer)
              }
              .clickable {
                coroutineScope.launch {
                  val bitmap = graphicsLayer.toImageBitmap()
                  onSave(bitmap)
                }
              }
              .background(Color.White)) {
        content()
      }
}

@Composable
fun ScrollingBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    BoxWithConstraints {
      val vscrollState = rememberScrollState()
      Box(modifier = modifier.padding(10.dp).fillMaxSize().verticalScroll(state = vscrollState)) {
        content()
      }
    }
