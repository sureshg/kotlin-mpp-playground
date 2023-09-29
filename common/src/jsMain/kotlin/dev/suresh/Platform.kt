package dev.suresh

import kotlinx.browser.window

actual val platform: TargetPlatform = JsPlatform

object JsPlatform : TargetPlatform {
  override val name: String = "JS"

  override val osInfo: Map<String, String?>
    get() =
        super.osInfo +
            mapOf(
                "name" to window.navigator.platform,
                "userAgent" to window.navigator.userAgent,
                "vendor" to window.navigator.vendor,
            )
}
