import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.suresh.Greeting
import dev.suresh.compose.res.*
import model.BirdsViewModel
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun CommonApp() {
  MaterialTheme {
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
