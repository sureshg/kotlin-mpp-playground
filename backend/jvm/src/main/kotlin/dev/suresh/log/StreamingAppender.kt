package dev.suresh.log

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.UnsynchronizedAppenderBase
import ch.qos.logback.core.encoder.Encoder
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Logback appender that broadcasts formatted log events via [SharedFlow]. Configure encoder in
 * logback.xml to reuse existing formatting.
 */
class StreamingAppender : UnsynchronizedAppenderBase<ILoggingEvent>() {

  var encoder: Encoder<ILoggingEvent>? = null

  override fun append(event: ILoggingEvent) {
    val bytes = encoder?.encode(event) ?: return
    logs.tryEmit(bytes.decodeToString().trimEnd())
  }

  companion object {

    val logs: SharedFlow<String>
      field =
          MutableSharedFlow(
              replay = 50,
              extraBufferCapacity = 500,
              onBufferOverflow = BufferOverflow.DROP_OLDEST,
          )
  }
}
