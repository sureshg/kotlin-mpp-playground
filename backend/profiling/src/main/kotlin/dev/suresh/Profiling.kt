package dev.suresh

import Arguments
import FlameGraph
import com.sun.management.HotSpotDiagnosticMXBean
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.management.ManagementFactory
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import jdk.jfr.Configuration
import jdk.jfr.FlightRecorder
import jdk.jfr.consumer.RecordingStream
import jfr2flame
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.pathString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import one.jfr.JfrReader

object Profiling {

  private val log = KotlinLogging.logger {}

  private const val diagnosticObjName = "com.sun.management:type=HotSpotDiagnostic"

  suspend fun threaddump(): Path = runOnVirtualThread {
    val server = ManagementFactory.getPlatformMBeanServer()
    val hotspot =
        ManagementFactory.newPlatformMXBeanProxy(
            server, diagnosticObjName, HotSpotDiagnosticMXBean::class.java)

    val heapDumpPath = createTempFile("heapdump", ".hprof")
    heapDumpPath.deleteIfExists()
    // hotspot.dumpThreads(ThreadDumpFormat.valueOf())
    hotspot.dumpHeap(heapDumpPath.pathString, true)
    heapDumpPath
  }

  suspend fun heapdump(): Path = runOnVirtualThread {
    val server = ManagementFactory.getPlatformMBeanServer()
    val hotspot =
        ManagementFactory.newPlatformMXBeanProxy(
            server, diagnosticObjName, HotSpotDiagnosticMXBean::class.java)

    val heapDumpPath = createTempFile("heapdump", ".hprof")
    heapDumpPath.deleteIfExists()
    hotspot.dumpHeap(heapDumpPath.pathString, true)
    heapDumpPath
  }

  suspend fun jfrSnapshot(maxAge: Duration = 2.minutes, maxSizeBytes: Long = 100_000_000): Path =
      runOnVirtualThread {
        val jfrPath = createTempFile("profile", ".jfr")
        val flightRecorder = FlightRecorder.getFlightRecorder()
        when {
          flightRecorder.recordings.isEmpty() ->
              RecordingStream(Configuration.getConfiguration("profile")).use {
                it.setMaxSize(maxSizeBytes)
                it.setMaxAge(maxAge.toJavaDuration())
                it.enable("jdk.CPULoad").withPeriod(100.milliseconds.toJavaDuration())
                it.enable("jdk.JavaMonitorEnter").withStackTrace()
                it.startAsync()
                Thread.sleep(5.seconds.toJavaDuration())
                it.dump(jfrPath)
              }
          else ->
              flightRecorder.takeSnapshot().use {
                if (it.size > 0) {
                  it.maxSize = maxSizeBytes
                  it.maxAge = maxAge.toJavaDuration()
                  it.dump(jfrPath)
                }
              }
        }
        log.info { "JFR file written to ${jfrPath.toAbsolutePath()}" }
        jfrPath
      }

  suspend fun flameGraph(maxAge: Duration = 2.minutes): String = runOnVirtualThread {
    val jfrPath = jfrSnapshot(maxAge = maxAge, maxSizeBytes = 100_000_000)
    JfrReader(jfrPath.pathString).use {
      val jfr2flame = jfr2flame(it, Arguments())
      val flameGraph = FlameGraph()
      jfr2flame.convert(flameGraph)
      val bos = ByteArrayOutputStream()
      flameGraph.dump(PrintStream(bos))
      jfrPath.deleteIfExists()
      bos.toString(StandardCharsets.UTF_8)
    }
  }
}
