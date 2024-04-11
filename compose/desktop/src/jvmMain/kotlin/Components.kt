import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))

fun Modifier.debug(color: Color = Color.Red) =
    then(
        if (debug)
            drawBehind {
              drawRoundRect(color = color, style = stroke, cornerRadius = CornerRadius(8.dp.toPx()))
            }
        else this)

@Composable
fun scrollingBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    BoxWithConstraints {
      val vscrollState = rememberScrollState()
      Box(
          modifier =
              modifier.fillMaxSize().padding(10.dp).debug().verticalScroll(state = vscrollState)) {
            content()
          }
    }
