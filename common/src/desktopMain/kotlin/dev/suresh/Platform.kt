package dev.suresh

actual fun platform(): Platform = DesktopPlatform

object DesktopPlatform : Platform {
  override val name: String = "Desktop JVM"
}
