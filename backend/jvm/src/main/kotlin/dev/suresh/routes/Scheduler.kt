package dev.suresh.routes

import dev.suresh.virtualThreadScope
import io.github.kevincianfarini.cardiologist.*
import io.ktor.server.application.*
import io.ktor.util.logging.*
import io.opentelemetry.instrumentation.annotations.*
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.*
import kotlinx.datetime.*

fun Application.scheduledTasks() {
  log.info("Starting scheduled tasks...")
  virtualThreadScope.launch {
    Clock.System.fixedPeriodPulse(10.seconds).beat(PulseBackpressureStrategy.SkipNext) { scheduled
      ->
      context(log) { task("Task at ${scheduled.toLocalDateTime(TimeZone.currentSystemDefault())}") }
    }
  }
}

@WithSpan("scheduled-task")
context(log: Logger)
fun task(name: String) {
  try {
    log.warn("Running $name")
    // log.info("Scheduled task: ${MediaApiClient().images().size}")
  } catch (e: Exception) {
    log.error("Failed to run scheduled task: ${e.message}", e)
  }
}
