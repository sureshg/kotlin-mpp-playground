import dev.suresh.Greeting
import kotlin.js.Promise
import kotlin.time.Duration.Companion.seconds
import kotlinx.browser.document
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.dom.appendText
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToString

suspend fun main() {
  val text = "${BuildConfig.time}: Hello, ${Greeting().greeting()}!"
  println(text)
  val root = document.getElementById("root")
  root?.appendText(text)
  delay(1.seconds)
  val promise = Promise.resolve("Promise")
  root?.appendText(promise.await())

  val ba = ByteString("Kotlin-IO".encodeToByteArray())
  println(ba.decodeToString())
}
