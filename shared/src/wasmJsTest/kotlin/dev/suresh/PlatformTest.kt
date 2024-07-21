package dev.suresh

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTest {

  @Test
  fun greetings() {
    assertTrue(Greeting().greeting().contains("Wasm"), "Check Wasm is mentioned")
  }
}
