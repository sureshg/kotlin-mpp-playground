package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.layer.drawLayer
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.*

/**
 * Image decoders for [ByteArray]
 *
 * @see [JPEG, PNG, BMP, WEBP to ImageBitmap][ByteArray.decodeToImageBitmap]
 * @see [Vector XML file to an ImageVector][ByteArray.decodeToImageVector]
 * @see [SVG file to a compose Painter][ByteArray.decodeToSvgPainter]
 */
fun ImageBitmap.toByteArray(): ByteArray? = asSkiaBitmap().readPixels()

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
