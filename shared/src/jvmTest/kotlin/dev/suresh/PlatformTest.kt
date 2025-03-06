package dev.suresh

import dev.suresh.http.MediaApiClient
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class PlatformTest {

  @Test
  fun greetings() {
    assertTrue(Greeting().greeting().contains("JVM"), message = "JVM platform check failed!")
    DOP.run()
  }

  @Test
  fun httpClientTest() = runTest {
    MediaApiClient().images().forEach { s -> println(s) }
    println("Done")
  }
}
