package hljs

import org.w3c.dom.*

@JsName("hljs")
@JsModule("highlight.js")
@JsNonModule
external class HighlightJs {
  companion object {
    fun highlightElement(block: HTMLElement)

    fun highlightAll()

    fun listLanguages(): List<String>

    fun autoDetection(languageName: String): Boolean

    val versionString: String
  }
}
