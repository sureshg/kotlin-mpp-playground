package dev.suresh

import BuildConfig

expect val platform: String

class Greeting {
  fun greeting() = "${BuildConfig.time} - ${KData("sdsds",123,"dssdsd")}: Kotlin $platform!"
}
