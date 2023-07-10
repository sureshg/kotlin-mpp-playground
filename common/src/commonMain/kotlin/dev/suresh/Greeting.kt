package dev.suresh

import BuildConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.bytestring.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Greeting {

  val json = Json {
    isLenient = true
    prettyPrint = true
  }

  fun greeting() =
      """
      | ${BuildConfig.time} - ${KData("Foo", 20, "test")}: Kotlin $platform: ${KotlinVersion.CURRENT}!
      | ${kotlinxTests()}
      """
          .trimMargin()

  private fun kotlinxTests(): String {
    val ba = "Kotlinx".encodeToByteArray()
    val bs1 = ByteString(ba)
    val bs2 = "IO".encodeToByteString()

    val bs = buildByteString {
      append(bs1)
      append(bs2)
    }

    return """
      |${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())}
      |${json.encodeToString(KData("Bar", 22, "test"))}
      |${bs.decodeToString()}
    """
        .trimMargin()
  }
}
