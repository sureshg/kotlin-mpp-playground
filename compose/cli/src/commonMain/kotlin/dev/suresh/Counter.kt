package dev.suresh

import androidx.compose.runtime.*
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import kotlinx.coroutines.delay

@Composable
fun Counter() {
  var counter by remember { mutableStateOf(0) }
  Text("The count is: $counter")

  Column { Box { Text("The count is: $counter") } }

  LaunchedEffect(Unit) {
    for (i in 1..10) {
      counter = i
      delay(1000)
    }
  }
}
