package dev.suresh

actual fun platform(): Platform = JsPlatform

object JsPlatform : Platform {
  override val name: String = "JS"
}
