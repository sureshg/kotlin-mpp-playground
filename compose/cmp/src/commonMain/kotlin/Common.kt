import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

val debug = true

val mainScope = MainScope()

fun Modifier.debug(color: Color = Color.Red) =
    then(if (debug) dashedBorder(1.dp, color, 8.dp) else this)

fun Modifier.dashedBorder(strokeWidth: Dp, color: Color, cornerRadius: Dp): Modifier = composed {
  val density = LocalDensity.current
  val strokeWidthPx = density.run { strokeWidth.toPx() }
  val cornerRadiusPx = density.run { cornerRadius.toPx() }

  then(
      Modifier.drawWithCache {
        onDrawBehind {
          val stroke =
              Stroke(
                  width = strokeWidthPx,
                  pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
          drawRoundRect(color = color, style = stroke, cornerRadius = CornerRadius(cornerRadiusPx))
        }
      })
}

fun Modifier.flipped() = then(FlippedModifier())

@Composable
fun ScrollingBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    BoxWithConstraints {
      val vscrollState = rememberScrollState()
      Box(modifier = modifier.padding(10.dp).fillMaxSize().verticalScroll(state = vscrollState)) {
        content()
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

class FlippedModifier : DrawModifier {
  override fun ContentDrawScope.draw() {
    scale(1f, -1f) { this@draw.drawContent() }
  }
}
