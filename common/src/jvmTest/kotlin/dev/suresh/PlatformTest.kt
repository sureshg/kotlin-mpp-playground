package dev.suresh

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlatformTest {

  @Test
  fun greetings() {
    assertTrue(Greeting().greeting().contains("JVM")) { "Check jvm is mentioned" }
  }
}
