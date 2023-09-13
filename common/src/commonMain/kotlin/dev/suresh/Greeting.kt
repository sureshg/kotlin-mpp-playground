package dev.suresh

import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.bytestring.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable data class KData(val name: String, val age: Int, @Redacted val password: String)

class Greeting {

  val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
  }

  fun greeting() = buildString {
    platform().buildInfo.forEach { (k, v) -> appendLine("$k : $v") }
    appendLine(KData("Foo", 20, "test"))
    appendLine(kotlinxTests())
  }

  private fun kotlinxTests(): String {
    val ba = "Kotlinx".encodeToByteArray()
    val bs1 = ByteString(data = ba)
    val bs2 = "IO".encodeToByteString()

    val bs = buildByteString {
      append(bs1)
      append(" ".encodeToByteArray())
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
