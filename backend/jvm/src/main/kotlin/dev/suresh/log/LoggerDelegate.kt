package dev.suresh.log

import dev.suresh.log
import io.github.oshai.kotlinlogging.KLogger
import java.io.Writer

class LoggerDelegate(val out: Writer, val logger: KLogger = log) : KLogger by logger {
  override fun info(message: () -> Any?) {
    super.info(message)
    out.appendLine(message().toString())
  }
}
