import dev.suresh.Greeting
import dev.suresh.flow.timerComposeFlow
import dev.suresh.http.MediaApiClient
import dev.suresh.log
import interop.topLevelJsFun
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
import xterm.ITerminalOptions
import xterm.ITheme
import xterm.Terminal

val mainScope = MainScope()

suspend fun main() {
  val root = document.getElementById("root") as HTMLDivElement
  val copy = document.getElementById("copy") as HTMLButtonElement

  copy.onclick = {
    mainScope.launch {
      navigator.clipboard.writeTextAsync(root.textContent.orEmpty()).await()
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
  // val promise = Promise.resolve("Promise")
  // root.appendText(promise.await())

  mainScope.launch {
    try {
      log.info { "Calling Media API client..." }
      val response = MediaApiClient().images()
      log.info { "Response: $response" }
    } catch (e: Throwable) {
      log.error(e) { "Error calling Media API client" }
    }
  }

  mainScope.launch {
    log.info { "Starting the timer..." }
    val timer = document.getElementById("timer") as HTMLDivElement
    timerComposeFlow().collectLatest { timer.innerText = it.toString() }
  }

  topLevelJsFun()
  runTerminal()
  runCoroutines()
}

fun runTerminal() {
  val term =
      Terminal(
          options =
              object : ITerminalOptions {
                init {
                  fontFamily = "Cascadia Code, Menlo, monospace"
                  theme =
                      object : ITheme {
                        init {
                          foreground = "#F8F8F8"
                          background = "#2D2E2C"
                          selectionBackground = "#5DA5D533"
                          black = "#1E1E1D"
                          brightBlack = "#262625"
                          red = "#CE5C5C"
                          brightRed = "#FF7272"
                          green = "#5BCC5B"
                          brightGreen = "#72FF72"
                          yellow = "#CCCC5B"
                          brightYellow = "#FFFF72"
                          blue = "#5D5DD3"
                          brightBlue = "#7279FF"
                          magenta = "#BC5ED1"
                          brightMagenta = "#E572FF"
                          cyan = "#5DA5D5"
                          brightCyan = "#72F0FF"
                          white = "#F8F8F8"
                          brightWhite = "#FFFFFF"
                        }
                      }
                  cursorBlink = true
                  allowProposedApi = true
                }
              })

  term.run {
    open(document.getElementById("terminal") as HTMLDivElement)
    write("Hello from \u001B[1;3;31mKotlin Multiplatform\u001B[0m \r\n\$ ")
    focus()
    onKey { kbEvt, _ ->
      val evt = kbEvt.domEvent
      val printable = !evt.altKey && !evt.ctrlKey && !evt.metaKey
      log.info { "Key pressed: ${evt.key} (${evt.keyCode})" }
      when {
        // Enter key (13)
        evt.keyCode == 13 -> term.write("\r\n$ ")
        // Do not delete the prompt on Backspace(8)
        evt.keyCode == 8 -> if (term.buffer.active.cursorX.toInt() > 2) term.write("\b \b")
        printable -> term.write(kbEvt.key)
      }
    }
  }
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
        val pause = Random.nextLong(10, 100)

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
