package dev.suresh

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTest {

  @Test
  fun greetings() {
    assertTrue(Greeting().greeting().contains("js"), "Check js is mentioned")
  }
}
