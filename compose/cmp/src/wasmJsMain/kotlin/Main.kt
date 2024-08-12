import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  hideSpinner()
  ComposeViewport(document.body!!) { App() }
}

fun hideSpinner() {
  val spinner = document.querySelector(".loader") as? HTMLElement
  spinner?.style?.display = "none"
}
