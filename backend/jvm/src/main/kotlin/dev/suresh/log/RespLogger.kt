package dev.suresh.log

import dev.suresh.log
import io.github.oshai.kotlinlogging.KLogger
import java.io.Writer

class RespLogger(private val out: Writer, private val logger: KLogger = log) : KLogger by logger {
  override fun info(message: () -> Any?) {
    super.info(message)
    out.appendLine(message().toString())
  }
}
