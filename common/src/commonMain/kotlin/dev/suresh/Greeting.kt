package dev.suresh

import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.io.bytestring.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable data class KData(val name: String, val age: Int, @Redacted val password: String)

class Greeting {

  fun greeting() = buildString {
    appendLine(json.encodeToString(platform().info))
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
    return bs.decodeToString()
  }
}
