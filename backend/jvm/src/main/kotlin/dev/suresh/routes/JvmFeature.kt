package dev.suresh.routes

import dev.suresh.lang.FFM
import dev.suresh.lang.VThread
import dev.suresh.log.LoggerDelegate
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val logger = KotlinLogging.logger {}

fun Route.jvmFeatures() {
  get("/ffm") {
    call.respondTextWriter { with(LoggerDelegate(this, logger)) { FFM.memoryLayout() } }
  }

  get("/vthreads") {
    call.respondTextWriter { with(LoggerDelegate(this, logger)) { VThread.virtualThreads() } }
  }
}
