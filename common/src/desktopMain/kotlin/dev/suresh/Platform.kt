package dev.suresh

actual val platform: Platform = DesktopPlatform

object DesktopPlatform : Platform {
  override val name: String = "Desktop JVM"
}
