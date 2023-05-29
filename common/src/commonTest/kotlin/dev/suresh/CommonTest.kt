package dev.suresh

import kotlin.test.Test
import kotlin.test.assertTrue

class CommonTest {

  @Test
  fun greetings() {
    assertTrue(
        actual = Greeting().greeting().contains("Kotlin"), message = "Check 'Kotlin' is mentioned")
  }
}
