package dev.suresh

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

object TestLogger : Logger by LoggerFactory.getLogger("ktor.test") {

  private val _messages = mutableListOf<String>()

  val messages: List<String>
    get() = _messages

  override fun trace(message: String?) = add("TRACE: $message")

  override fun debug(message: String?) = add("DEBUG: $message")

  override fun debug(message: String?, cause: Throwable) = add("DEBUG: $message")

  override fun info(message: String?) = add("INFO: $message")

  private fun add(message: String?) {
    if (message != null) {
      val mdcText =
          MDC.getCopyOfContextMap()?.let { mdc ->
            if (mdc.isNotEmpty()) {
              mdc.entries
                  .sortedBy { it.key }
                  .joinToString(prefix = " [", postfix = "]") { "${it.key}=${it.value}" }
            } else {
              ""
            }
          } ?: ""
      _messages.add("$message$mdcText")
    }
  }

  fun clear() = _messages.clear()
}
