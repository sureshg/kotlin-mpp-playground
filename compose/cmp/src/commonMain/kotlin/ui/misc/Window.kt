package ui.misc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun WindowSizeClass() {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    val size = calculateWindowSizeClass()
    Text("width = ${size.widthSizeClass}\nheight = ${size.heightSizeClass}")
  }
}
