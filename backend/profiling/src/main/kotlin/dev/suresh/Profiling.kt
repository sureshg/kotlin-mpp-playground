@file:Suppress("DuplicatedCode")

package dev.suresh

import BuildConfig
import com.sun.management.HotSpotDiagnosticMXBean
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.ByteArrayOutputStream
import java.lang.management.ManagementFactory
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import jdk.jfr.Configuration
import jdk.jfr.FlightRecorder
import jdk.jfr.consumer.RecordingStream
import jdk.management.VirtualThreadSchedulerMXBean
import kotlin.io.path.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import me.saket.bytesize.binaryBytes
import one.convert.*
import one.jfr.JfrReader

object Profiling {

  private val log = KotlinLogging.logger {}

  const val diagnosticObjName = "com.sun.management:type=HotSpotDiagnostic"

  val virtualThreadMxBean by lazy {
    ManagementFactory.getPlatformMXBean(VirtualThreadSchedulerMXBean::class.java)
  }

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
        log.info {
          "JFR file written to ${jfrPath.toAbsolutePath()} (${jfrPath.fileSize().binaryBytes})"
        }
        jfrPath
      }

  suspend fun convertJfr(format: String = "html", maxAge: Duration = 2.minutes): String =
      runOnVirtualThread {
        val jfrPath = jfrSnapshot(maxAge, 100_000_000)
        val args = Arguments("--output", format, "--reverse", "--title", BuildConfig.name)

        val converter =
            JfrReader(jfrPath.pathString).use {
              when (format) {
                "heatmap" -> JfrToHeatmap(it, args)
                "html" -> JfrToFlame(it, args)
                else -> error("Unsupported format: $format")
              }.apply { convert() }
            }

        val result =
            ByteArrayOutputStream().use { out ->
              when (converter) {
                is JfrToHeatmap -> converter.dump(out)
                is JfrToFlame -> converter.dump(out)
                else -> error("Unsupported converter for $format")
              }
              out.toString(StandardCharsets.UTF_8)
            }
        jfrPath.deleteIfExists()
        result
      }
}
