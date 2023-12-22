package dev.suresh.routes

import dev.suresh.lang.FFM
import dev.suresh.lang.JFR
import dev.suresh.lang.VThread
import dev.suresh.log.RespLogger
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.Writer

private val logger = KotlinLogging.logger {}

fun Route.jvmFeatures() {
  get("/ffm") {
    call.respondLogStream {
      FFM.memoryLayout()
    }
  }

  get("/vthreads") {
    call.respondLogStream {
      VThread.virtualThreads()
    }
  }

  get("/jfr") {
    call.respondLogStream {
      JFR.recordingStream()
    }
  }
}

suspend fun ApplicationCall.respondLogStream(
    contentType: ContentType = ContentType.Text.Plain,
    block: suspend context(KLogger) Writer.() -> Unit
) {
  respondTextWriter(contentType = contentType) {
    block(RespLogger(this, logger), this)
  }
}
