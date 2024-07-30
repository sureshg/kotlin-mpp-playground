package service

import java.io.File
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

interface XvfbParameters : BuildServiceParameters {
  val executable: Property<String>
  val arguments: ListProperty<String>
}

/**
 * To launch Xvfb server from a Gradle build, use the following code:
 * ```kotlin
 *  val xvfbServer by lazy {
 *     gradle.sharedServices.registerIfAbsent("xvfb", XvfbServer::class) {
 *       parameters.executable = "Xvfb"
 *       parameters.arguments.empty()
 *     }
 *  }
 *
 *  withType<Test>().configureEach {
 *    usesService(xvfbServer)
 *    // https://github.com/gradle/gradle/issues/4224
 *    environment("DISPLAY", object { override fun toString() = xvfbServer.get().display })
 *  }
 * ```
 */
abstract class XvfbServer : BuildService<XvfbParameters>, AutoCloseable {

  private val xvfbProcess =
      ProcessBuilder()
          .command(
              parameters.executable.get(),
              "-displayfd",
              "1",
              *parameters.arguments.get().toTypedArray())
          .redirectInput(ProcessBuilder.Redirect.from(File("/dev/null")))
          .start()

  private val _display = CompletableFuture<String>()

  init {
    val logger = Logging.getLogger(XvfbServer::class.java)
    logger.info("Xvfb: PID=${xvfbProcess.pid()}")

    thread {
      xvfbProcess.inputStream.reader().useLines { lines ->
        lines.fold(true) { isFirst, line ->
          if (isFirst) {
            logger.info("Xvfb: DISPLAY=:$line")
            _display.complete(":$line")
          } else logger.info("Xvfb: out=$line")
          false
        } && _display.completeExceptionally(IllegalStateException("No display"))
      }
    }

    thread {
      xvfbProcess.errorStream.reader().useLines { lines ->
        for (line in lines) logger.info("Xvfb: err=$line")
      }
    }
  }

  val display: String
    get() = _display.get()

  override fun close() {
    xvfbProcess.destroy()
  }
}
