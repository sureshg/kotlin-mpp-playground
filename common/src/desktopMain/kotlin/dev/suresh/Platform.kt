package dev.suresh

actual val platform: TargetPlatform = DesktopPlatform

object DesktopPlatform : TargetPlatform {
  override val name: String = "Desktop JVM"
}
