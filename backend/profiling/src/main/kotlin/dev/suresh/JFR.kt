package dev.suresh

import BuildConfig
import io.github.oshai.kotlinlogging.KLogger
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import jdk.jfr.*
import jdk.jfr.consumer.EventStream
import jdk.jfr.consumer.RecordingStream
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Name("dev.suresh.Counter")
@Label("App Counter")
@Description("App Counter Event")
@Category("Services", BuildConfig.name)
@Period("1 s")
@StackTrace(false)
class Counter(@Label("Count") private var count: Long = 0) : Event() {
  fun inc() = count++
}

/** Check [JFR Events](https://sap.github.io/SapMachine/jfrevents/) for more details. */
object JFR {

  private val started = AtomicBoolean(false)

  context(KLogger)
  suspend fun recordingStream() = runOnVirtualThread {
    if (!started.getAndSet(true)) {
      info { "Adding periodic JFR event..." }
      addPeriodicJFREvent(Counter()) { inc() }
    }

    val config = Configuration.getConfiguration("profile")
    RecordingStream(config).use {
      info { "RecordingStream started!" }
      it.setMaxSize(5_000_000)
      it.enable("jdk.CPULoad").withPeriod(Duration.ofSeconds(1))
      it.onEvent("jdk.CPULoad") { event ->
        val jvmUser = event.getFloat("jvmUser")
        val jvmSystem = event.getFloat("jvmSystem")
        val machineTotal = event.getFloat("machineTotal")
        info {
          "JVM User: %1$.2f, JVM System: %2$.2f, Machine Total: %3$.2f"
              .format(jvmUser, jvmSystem, machineTotal)
        }

        // if (jvmUser > 0.8) {
        //  it.dump(Path("cpu-load.jfr"))
        // }
      }

      // Contended classes for more than 10ms
      it.enable("jdk.JavaMonitorEnter").withThreshold(Duration.ofMillis(10))
      it.onEvent("jdk.JavaMonitorEnter") { event ->
        info { "Long held Monitor: ${event.getClass("monitorClass")}" }
      }

      it.enable("jdk.GarbageCollection")
      it.enable("jdk.JVMInformation")
      it.onEvent("jdk.JVMInformation") { event ->
        val jvmName = event.getString("jvmName")
        val jvmVersion = event.getString("jvmVersion")
        info { "JVM: $jvmName, Version: $jvmVersion" }
      }

      it.enable("dev.suresh.Counter")
      it.onEvent("dev.suresh.Counter") { event ->
        val duration = event.duration.toMillis()
        info { "Count: ${event.getLong("count")}, duration: $duration" }

        // Find correlation events by getting an event window 1 sec before and after the event.
        if (duration > 500) {
          EventStream.openRepository().use { es ->
            es.setStartTime(event.startTime.minus(Duration.ofSeconds(1)))
            es.setEndTime(event.endTime.plus(Duration.ofSeconds(1)))
            es.onEvent("jdk.GCPhasePause") { gcEvent ->
              val gcDuration = gcEvent.duration.toMillis()
              info { "GC pause of $gcDuration millis during the Counter event!" }
            }
          }
        }
      }

      it.startAsync()
      Thread.sleep(3.seconds.toJavaDuration())
    }
    info { "RecordingStream done!" }
  }
}
