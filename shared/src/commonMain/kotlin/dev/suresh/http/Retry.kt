package dev.suresh.http

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class Timeout(val connection: Duration, val read: Duration, val write: Duration) {
  companion object {
    val DEFAULT = Timeout(connection = 5.seconds, read = 5.seconds, write = 5.seconds)
  }
}

data class Retry(val attempts: Int, val maxDelay: Duration) {
  companion object {
    val DEFAULT = Retry(attempts = 2, maxDelay = 2.seconds)
  }
}
