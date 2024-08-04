package dev.suresh

import kotlin.native.Platform as KNPlatform
import kotlinx.cinterop.*
import platform.posix.*

actual val platform: Platform = NativePlatform

object NativePlatform : Platform {
  override val name: String = "Native"

  override fun env(key: String, def: String?) = getenv(key)?.toKStringFromUtf8() ?: def

  override val osInfo: Map<String, String?>
    get() =
        super.osInfo +
            super.osInfo +
            mapOf(
                "name" to KNPlatform.osFamily.name,
                "version" to env("OSTYPE", "n/a"),
                "arch" to KNPlatform.cpuArchitecture.name,
                "user" to env("USER"),
            )
}
