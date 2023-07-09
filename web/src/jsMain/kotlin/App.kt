import dev.suresh.Greeting
import kotlin.js.Promise
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.browser.document
import kotlinx.coroutines.*
import kotlinx.dom.appendText
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.progress
import org.w3c.dom.HTMLProgressElement
import org.w3c.dom.Node

suspend fun main() {
  val text = Greeting().greeting()
  val root = document.getElementById("root")

  text.lines().forEach {
    println(it)
    root?.appendText(it)
    root?.appendChild(document.createElement("br"))
  }

  delay(1.seconds)
  val promise = Promise.resolve("Promise")
  root?.appendText(promise.await())

  topLevelJsFun()
  runCoroutines()
}

suspend fun runCoroutines() {
  val coroutinesElm = document.getElementById("coroutines")
  coroutineScope {
    val progressElms =
        listOf("primary", "link", "success", "warning", "danger", "info").map {
          val progress =
              document.create.progress(classes = "progress is-$it is-small") {
                max = "100"
                value = "0"
              } as HTMLProgressElement
          coroutinesElm?.appendChild(progress)
          progress
        }

    progressElms.forEach { progress ->
      launch {
        var cancelled = false
        val pause = Random.nextLong(10, 300)

        progress.onclick = {
          cancelled = !cancelled
          null
        }

        while (cancelled.not()) {
          delay(pause.milliseconds)
          progress.value++
          if (progress.value >= progress.max) {
            break
          }
        }
      }
    }
  }

  val notification =
      document.create.div(classes = "notification is-primary is-light") {
        +"All coroutines are completed!"
      }
  coroutinesElm?.appendChild(notification)
}

fun Node.sayHello() {
  append { div { +"Hello Kotlin ${KotlinVersion.CURRENT}" } }
}
