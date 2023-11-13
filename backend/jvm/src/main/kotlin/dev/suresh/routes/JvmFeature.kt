package dev.suresh.routes

import dev.suresh.lang.FFM
import dev.suresh.lang.VThread
import dev.suresh.log.LoggerDelegate
import dev.suresh.runOnVirtualThread
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.PrintStream
import jdk.jfr.Configuration
import jdk.jfr.consumer.RecordingStream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import one.converter.Arguments
import one.converter.FlameGraph
import one.converter.jfr2flame
import one.jfr.JfrReader
import one.profiler.AsyncProfiler
import one.profiler.AsyncProfilerLoader
import one.profiler.Events

private val logger = KotlinLogging.logger {}

val mutex = Mutex()

val profiler: AsyncProfiler? by lazy {
  val ap = AsyncProfilerLoader.loadOrNull()
  ap.start(Events.CPU, 1000)
  ap
}

fun Route.jvmFeatures() {
  get("/ffm") {
    call.respondTextWriter { with(LoggerDelegate(this, logger)) { FFM.memoryLayout() } }
  }

  get("/vthreads") {
    call.respondTextWriter { with(LoggerDelegate(this, logger)) { VThread.virtualThreads() } }
  }

  get("/profile") {
    // Run the blocking operation on virtual thread and make sure
    // only one profile operation is running at a time.
    when {
      mutex.isLocked -> call.respondText("Profile operation is already running")
      else ->
          mutex.withLock {
            runOnVirtualThread {
              val jfrPath = kotlin.io.path.createTempFile("profile", ".jfr")
              RecordingStream(Configuration.getConfiguration("profile")).use {
                it.enable("jdk.CPULoad").withPeriod(100.milliseconds.toJavaDuration())
                it.enable("jdk.JavaMonitorEnter").withStackTrace()
                it.startAsync()
                Thread.sleep(5_000)
                it.dump(jfrPath)
                println("JFR file written to ${jfrPath.toAbsolutePath()}")
              }

              when (call.request.queryParameters.contains("download")) {
                true -> {
                  call.response.header(
                      HttpHeaders.ContentDisposition,
                      ContentDisposition.Attachment.withParameter(
                              ContentDisposition.Parameters.FileName, jfrPath.fileName.name)
                          .toString())
                  call.respondFile(jfrPath.toFile())
                }
                else -> {
                  val jfr2flame = jfr2flame(JfrReader(jfrPath.pathString), Arguments())
                  val flameGraph = FlameGraph()
                  jfr2flame.convert(flameGraph)

                  call.respondOutputStream(contentType = ContentType.Text.Html) {
                    flameGraph.dump(PrintStream(this))
                  }
                  jfrPath.deleteIfExists()
                }
              }
            }
          }
    }
  }
}
