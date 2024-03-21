import dev.suresh.platform
import kotlin.test.Test
import kotlin.test.assertTrue

class NativeTest {
  @Test
  fun test() {
    assertTrue(platform.name == "Native")
  }
}
