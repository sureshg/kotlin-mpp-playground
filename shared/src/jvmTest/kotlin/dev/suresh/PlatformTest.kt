package dev.suresh

import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class PlatformTest {

  @Test
  fun greetings() {
    assertTrue(Greeting().greeting().contains("JVM"), message = "JVM platform check failed!")
    DOP.run()
  }
}
