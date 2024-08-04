package ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import java.awt.image.BufferedImage
import java.net.URI
import java.nio.file.Path
import javax.imageio.ImageIO

fun URI.toImageBitmap() = ImageIO.read(toURL()).toComposeImageBitmap()

fun ImageBitmap.toPngImage(path: Path): Boolean =
    toAwtImage().let { ImageIO.write(it, "png", path.toFile()) }

fun Painter.toPngImage(path: Path): Boolean {
  val img = toAwtImage(Density(1f), LayoutDirection.Ltr)
  return BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB).run {
    graphics.drawImage(img, 0, 0, null)
    ImageIO.write(this, "png", path.toFile())
  }
}
