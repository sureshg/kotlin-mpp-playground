package ui.birds

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ui.debug
import ui.home.HomeButton

@Composable
fun BirdImages(modifier: Modifier = Modifier, navToHome: () -> Unit) {
  val birdsViewModel = viewModel { BirdsViewModel() }
  val state by birdsViewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()

  // Load the BirdUiState
  LaunchedEffect(birdsViewModel) { birdsViewModel.update() }

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .padding(10.dp)
              .debug(color = Color.Black)
              .verticalScroll(scrollState),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceEvenly) {
        if (state.images.isEmpty()) {
          CircularProgressIndicator(
              modifier = Modifier.width(64.dp),
              color = MaterialTheme.colorScheme.secondary,
              trackColor = MaterialTheme.colorScheme.surfaceVariant,
          )
          Text("Loading images...")
        } else {
          state.images.forEach { image ->
            SelectionContainer { Text(image.path) }
            Spacer(modifier = Modifier.width(5.dp))
          }
        }
        Spacer(modifier = Modifier.height(5.dp))
        HomeButton(navToHome)
      }
}
