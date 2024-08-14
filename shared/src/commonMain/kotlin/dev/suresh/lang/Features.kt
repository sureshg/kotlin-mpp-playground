import dev.suresh.log
import kotlin.io.encoding.Base64
import kotlin.jvm.JvmInline
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.TimeSource
import kotlin.uuid.Uuid

enum class Planet(val moon: Int) {
  MERCURY(0),
  VENUS(0),
  EARTH(1),
  MARS(2),
  JUPITER(5),
  SATURN(66),
  URANUS(62),
  NEPTUNE(13)
}

data object MyService

@JvmInline
value class Person(private val name: String) {
  init {
    check(name.isNotBlank()) { "Name should not be empty" }
  }

  constructor(firstName: String, lastName: String) : this("$firstName $lastName") {
    check(firstName.isNotBlank()) { "First name should not be empty" }
    check(lastName.isNotBlank()) { "Last name should not be empty" }
  }
}

fun langFeatures() {
  log.info { Planet.entries.filter { it.moon == 1 } }
  // MyService::class.createInstance() - Throws error
  log.info { MyService }

  log.info { Person("Foo") }
  log.info { Person("Foo", "Bar") }
}

fun stdlibFeatures() {

  val timeSource = TimeSource.Monotonic
  val m1 = timeSource.markNow()

  val m2 = timeSource.markNow()
  log.info { m2.elapsedNow() + 2.microseconds }
  log.info { m1 + 2.microseconds }
  log.info { m2 - m1 }

  val regex = """\b(?<city>[A-Za-z\s]+),\s(?<state>[A-Z]{2}),\s(?<areacode>[0-9]{5})\b""".toRegex()
  val match = regex.find("San Jose, CA, 95124")!!
  log.info { match.groups }
  log.info { match.groups["city"]?.value }
  log.info { match.groups["state"]?.value }
  log.info { match.groups["areacode"]?.value }

  val hexFormat = HexFormat {
    upperCase = true
    bytes {
      bytePrefix = "0x"
      byteSeparator = ":"
      bytesPerLine = 4
      bytesPerGroup = 2
      groupSeparator = " "
    }

    number {
      prefix = "0x"
      removeLeadingZeros = true
    }
  }

  log.info { 123232.toHexString(hexFormat) }
  val hex = "Kotlin ${KotlinVersion.CURRENT}".encodeToByteArray().toHexString(hexFormat)
  log.info { hex }
  log.info { hex.hexToByteArray(hexFormat).decodeToString() }

  log.info { Base64.Mime.encode("Hello Kotlin!".encodeToByteArray()) }
  log.info { "UUID: ${ Uuid.random()}" }
}
