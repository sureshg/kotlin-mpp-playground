import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.div

class AppTest {

  @Test
  fun testKotlinVersion() {
    val container = document.create.div {}
    container.sayHello()
    assertEquals("Hello Kotlin ${KotlinVersion.CURRENT}", container.textContent)
  }
}
