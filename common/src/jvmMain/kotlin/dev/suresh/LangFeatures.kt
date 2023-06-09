package dev.suresh

import kotlin.time.Duration.Companion.microseconds
import kotlin.time.TimeSource

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
  println(Planet.entries.filter { it.moon == 1 })
  // MyService::class.createInstance() - Throws error
  println(MyService)

  println(Person("Foo"))
  println(Person("Foo", "Bar"))
}

fun stdlibFeatures() {

  val timeSource = TimeSource.Monotonic
  val m1 = timeSource.markNow()
  Thread.sleep(100)
  val m2 = timeSource.markNow()
  println(m2.elapsedNow() + 2.microseconds)
  println(m1 + 2.microseconds)
  println(m2 - m1)

  val regex = """\b(?<city>[A-Za-z\s]+),\s(?<state>[A-Z]{2}),\s(?<areacode>[0-9]{5})\b""".toRegex()
  val match = regex.find("San Jose, CA, 95124")!!
  println(match.groups)
  println(match.groups["city"]?.value)
  println(match.groups["state"]?.value)
  println(match.groups["areacode"]?.value)

  val hexFormat = HexFormat {
    this.upperCase = true
    this.bytes {
      this.bytePrefix = "0x"
      this.byteSeparator = ":"
      this.bytesPerLine = 4
      this.bytesPerGroup = 2
      this.groupSeparator = " "
    }

    this.number {
      this.prefix = "0x"
      this.removeLeadingZeros = true
    }
  }

  println(123232.toHexString(hexFormat))
  val hex = "Kotlin 1.9.0".encodeToByteArray().toHexString(hexFormat)
  println(hex)
  println(hex.hexToByteArray(hexFormat).decodeToString())
}
