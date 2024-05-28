import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

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

@Composable
fun scrollingBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    BoxWithConstraints {
      val vscrollState = rememberScrollState()
      Box(modifier = modifier.padding(10.dp).fillMaxSize().verticalScroll(state = vscrollState)) {
        content()
      }
    }

fun Painter.toPngImage(file: File) {
  val img = toAwtImage(Density(1f), LayoutDirection.Ltr)
  BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB).run {
    graphics.drawImage(img, 0, 0, null)
    ImageIO.write(this, "png", file)
  }
}
