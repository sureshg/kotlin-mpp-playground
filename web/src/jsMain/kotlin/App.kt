import dev.suresh.Greeting
import dev.suresh.flow.timerComposeFlow
import dev.suresh.log
import js.promise.await
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.dom.appendText
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.progress
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLProgressElement
import org.w3c.dom.Node
import web.navigator.navigator

val mainScope = MainScope()

suspend fun main() {
  val root = document.getElementById("root") as HTMLDivElement
  val copy = document.getElementById("copy") as HTMLButtonElement

  copy.onclick = {
    mainScope.launch {
      navigator.clipboard.writeText(root.textContent.orEmpty()).await()
      log.info { "Copied to clipboard using kotlinx-wrapper APIs!" }
    }
  }

  val text = Greeting().greeting()
  text.lines().forEach {
    log.info { it }
    root.appendText(it)
    root.appendChild(document.createElement("br"))
  }
  // HighlightJs.highlightElement(root)

  // Javascript Promise
  // val promise = Promise.resolve("Promise")
  // root.appendText(promise.await())

  mainScope.launch {
    log.info { "Starting the timer..." }
    val timer = document.getElementById("timer") as HTMLDivElement
    timerComposeFlow().collectLatest { timer.innerText = it.toString() }
  }

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
