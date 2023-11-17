package dev.suresh.routes

import com.sun.management.HotSpotDiagnosticMXBean
import dev.suresh.jvmRuntimeInfo
import dev.suresh.plugins.debug
import dev.suresh.runOnVirtualThread
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.PrintStream
import java.lang.management.ManagementFactory
import jdk.jfr.Configuration
import jdk.jfr.consumer.RecordingStream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
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

private val DEBUG = ScopedValue.newInstance<Boolean>()

val mutex = Mutex()

val profiler: AsyncProfiler? by lazy {
  val ap = AsyncProfilerLoader.loadOrNull()
  ap.start(Events.CPU, 1000)
  ap
}

fun Route.mgmtRoutes() {

  get("/info") {
    call.respond(ScopedValue.where(DEBUG, call.debug).get { jvmRuntimeInfo(DEBUG.get()) })
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
                it.setMaxSize(100 * 1024 * 1024)
                it.setMaxAge(2.minutes.toJavaDuration())
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

  get("/heapdump") {
    val server = ManagementFactory.getPlatformMBeanServer()
    val hotspot =
        ManagementFactory.newPlatformMXBeanProxy(
            server,
            "com.sun.management:type=HotSpotDiagnostic",
            HotSpotDiagnosticMXBean::class.java)

    val heapDumpPath = kotlin.io.path.createTempFile("heapdump", ".hprof")
    heapDumpPath.deleteIfExists()
    hotspot.dumpHeap(heapDumpPath.pathString, true)
    call.response.header(
        HttpHeaders.ContentDisposition,
        ContentDisposition.Attachment.withParameter(
                ContentDisposition.Parameters.FileName, heapDumpPath.fileName.name)
            .toString())
    call.respondFile(heapDumpPath.toFile())
    heapDumpPath.deleteIfExists()
  }
}
