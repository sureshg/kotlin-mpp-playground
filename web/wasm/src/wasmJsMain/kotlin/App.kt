import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.dom.appendText
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLProgressElement

val mainScope = MainScope()

val log = KotlinLogging.logger {}

suspend fun main() {
  log.info { "Hello Kotlin WasmJS!" }
  coroutineScope {
    val coroutinesElm = document.getElementById("coroutines")
    val progressElms =
        listOf("primary", "link", "success", "warning", "danger", "info").map {
          val progress = document.createElement("progress") as HTMLProgressElement
          progress.apply {
            id = it
            max = 100.toDouble()
            value = 0.toDouble()
          }
          coroutinesElm?.appendChild(progress)
          progress
        }

    progressElms.forEach { progress ->
      launch {
        var cancelled = false
        val pause = Random.nextLong(10, 100)
        progress.onclick = { cancelled = !cancelled }
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

  val info = document.getElementById("info-box") as? HTMLDivElement
  info?.appendText("All coroutines are completed!")

  info?.onclick = {
    mainScope.launch {
      val files = document.selectFileFromDisk()
      info?.appendText("Selected files: $files")
      files.forEach { file ->
        val text = file.readAsText()
        info?.appendText("File content: $text")
      }
    }
  }
}
