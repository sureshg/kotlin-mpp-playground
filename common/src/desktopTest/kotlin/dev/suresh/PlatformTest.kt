package dev.suresh

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlatformTest {

  @Test
  fun greetings() {
    assertTrue(Greeting().greeting().contains("Desktop")) { "Desktop platform check failed!" }
  }
}
