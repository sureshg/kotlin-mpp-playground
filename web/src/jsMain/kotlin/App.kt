import dev.suresh.Greeting
import kotlin.js.Promise
import kotlin.time.Duration.Companion.seconds
import kotlinx.browser.document
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.dom.appendText
import kotlinx.html.div
import kotlinx.html.dom.append
import org.w3c.dom.Node

suspend fun main() {
  val text = "${BuildConfig.time}: Hello, ${Greeting().greeting()}!"
  val root = document.getElementById("root")

  text.lines().forEach {
    println(it)
    root?.appendText(it)
    root?.appendChild(document.createElement("br"))
  }

  delay(1.seconds)
  val promise = Promise.resolve("Promise")
  root?.appendText(promise.await())
}

fun Node.sayHello() {
  append { div { +"Hello Kotlin ${KotlinVersion.CURRENT}" } }
}
