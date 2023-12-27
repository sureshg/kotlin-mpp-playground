package dev.suresh

import kotlinx.browser.window

actual val platform: Platform = JsPlatform

object JsPlatform : Platform {
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
