package ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.MainScope

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

inline fun Modifier.ifTrue(value: Boolean, builder: Modifier.() -> Modifier) =
    then(if (value) builder() else Modifier)

fun Modifier.flipped() = then(FlippedModifier())

class FlippedModifier : DrawModifier {
  override fun ContentDrawScope.draw() {
    scale(1f, -1f) { this@draw.drawContent() }
  }
}
