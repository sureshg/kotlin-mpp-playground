package dev.suresh

import kotlin.concurrent.thread
import kotlin.test.assertEquals
import org.jetbrains.lincheck.Lincheck

class LincheckTests {

  // @Test
  fun test() =
      Lincheck.runConcurrentTest {
        var counter = 0
        // Increment the counter concurrently
        val t1 = thread { counter++ }
        val t2 = thread { counter++ }
        // Wait for the threads to finish
        t1.join()
        t2.join()
        // Check both increments have been applied
        assertEquals(2, counter)
      }
}
