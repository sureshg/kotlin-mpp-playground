package dev.suresh.gen

import java.awt.Color
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import javax.imageio.ImageIO
import kotlin.math.PI

object GenArt {

  const val S = 1024

  fun flower() {
    BufferedImage(S, S, TYPE_INT_ARGB).apply {
      createGraphics().apply {
        color = Color(0, 0, 0, 25)
        for (angle in 0..<360 step 15) {
          val old = transform
          val centerX = S / 2.0
          val centerY = S / 2.0
          val eWidth = S / 8.0
          val eHeight = S * 7.0 / 16
          translate(centerX, centerY)
          rotate(angle.toRadians())
          fill(Ellipse2D.Double(eHeight - centerX, eWidth - centerY, eWidth, eHeight))
          transform = old
        }
        dispose()
      }
      ImageIO.write(this, "png", java.io.File("flower.png"))
    }
  }
}

fun Double.toRadians() = this / 180 * PI

fun Int.toRadians() = toDouble().toRadians()

fun Double.toDegrees() = this * 180 / PI
