package ui.file

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.debug
import ui.home.HomeButton

@Composable expect fun DragDropListView()

@Composable
fun FileBrowser(modifier: Modifier = Modifier, navToHome: () -> Unit) {
  Column(
      modifier =
          modifier.fillMaxSize().padding(10.dp).debug(color = MaterialTheme.colorScheme.primary),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceEvenly) {
        DragDropListView()
        HomeButton(navToHome)
      }
}
