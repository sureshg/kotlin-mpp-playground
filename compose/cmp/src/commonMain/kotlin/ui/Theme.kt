package ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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

object FileColors {
  val default = Color.Gray
  val active = Color(29, 117, 223, 255)
  val fileItemBg = Color(233, 30, 99, 255)
  val fileItemFg = Color.White
}
