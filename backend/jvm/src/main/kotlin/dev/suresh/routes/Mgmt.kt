package dev.suresh.routes

import com.sun.management.HotSpotDiagnosticMXBean
import dev.suresh.jvmRuntimeInfo
import dev.suresh.plugins.debug
import dev.suresh.runOnVirtualThread
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.io.PrintStream
import java.lang.management.ManagementFactory
import jdk.jfr.Configuration
import jdk.jfr.consumer.RecordingStream
import kotlin.io.path.*
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

val docRoot = Path(System.getProperty("java.io.tmpdir"))

fun Route.mgmtRoutes() {

  staticFiles(remotePath = "/tmp", dir = docRoot.toFile())

  get("/info") {
    call.respond(ScopedValue.where(DEBUG, call.debug).get { jvmRuntimeInfo(DEBUG.get()) })
  }

  get("/browse/{param...}") {
    val reqPath =
        Path(call.parameters.getAll("param").orEmpty().joinToString(File.separator)).normalize()
    println("reqPath: $reqPath")
    val path = docRoot.resolve(reqPath)
    println("path: $path")
    when {
      path.exists() -> {
        when {
          path.isDirectory() -> {
            call.respondText(
                """
<!DOCTYPE html>
<html>
<head>
    <title>File Browser</title>
    <style>
        body {
            font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #FFFFFF;
            color: #333;
        }

        .container {
            max-width: 900px;
            margin: auto;
            padding: 1em;
        }

        h2 {
            font-weight: 500;
            margin-bottom: 1em;
        }

        ul {
            list-style: none;
            padding: 0;
            margin: 0;
        }

        ul li {
            display: flex;
            align-items: center;
            background-color: #F9F9F9;
            padding: 0.5em;
            margin: 0.2em 0;
            border-radius: 3px;
            transition: background 0.15s ease-in-out;
        }

        ul li:hover {
            background-color: #E9E9E9;
        }

        ul li a {
            text-decoration: none;
            color: #333;
        }

        ul li i {
            margin-right: 0.5em;
        }

        .fa-folder::before {
            content: url("data:image/svg+xml,%3Csvg fill='%23000000' viewBox='0 0 24 24' xmlns='http://www.w3.org/2000/svg' width='24' height='24'%3E%3Cpath d='M19 5.5h-6.28l-0.32 -1a3 3 0 0 0 -2.84 -2H5a3 3 0 0 0 -3 3v13a3 3 0 0 0 3 3h14a3 3 0 0 0 3 -3v-10a3 3 0 0 0 -3 -3Zm1 13a1 1 0 0 1 -1 1H5a1 1 0 0 1 -1 -1v-13a1 1 0 0 1 1 -1h4.56a1 1 0 0 1 0.95 0.68l0.54 1.64a1 1 0 0 0 0.95 0.68h7a1 1 0 0 1 1 1Z'/%3E%3C/svg%3E");
        }
        .fa-file::before {
            content: url("data:image/svg+xml,%3Csvg fill='%23000000' viewBox='0 0 24 24' xmlns='http://www.w3.org/2000/svg' width='24' height='24'%3E%3Cpath d='M20 8.94a1.31 1.31 0 0 0 -0.06 -0.27v-0.09a1.07 1.07 0 0 0 -0.19 -0.28l-6 -6a1.07 1.07 0 0 0 -0.28 -0.19h-0.09L13.06 2H7a3 3 0 0 0 -3 3v14a3 3 0 0 0 3 3h10a3 3 0 0 0 3 -3V8.94Zm-6 -3.53L16.59 8H14ZM18 19a1 1 0 0 1 -1 1H7a1 1 0 0 1 -1 -1V5a1 1 0 0 1 1 -1h5v5a1 1 0 0 0 1 1h5Z'/%3E%3C/svg%3E");
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>Directory listing for: ${reqPath}</h2>
        <ul>
        ${
                    path.toFile().list().orEmpty().joinToString("\n") { file ->
                        val icon = if (File(file).isDirectory) "<i class=\"fa-folder\"></i>" else "<i class=\"fa-file\"></i>"
                        "<li>${icon}<a href=\"/browse/${file}\">${file}</a></li>"
                    }
                }
        </ul>
    </div>
</body>
</html>
"""
                    .trimIndent(),
                contentType = ContentType.Text.Html,
                status = HttpStatusCode.OK)
          }
          else -> call.respondFile(path.toFile())
        }
      }
      else ->
          call.respondText(
              """
              |<h2>File not found</h2>
              |$reqPath <p>
              |"""
                  .trimMargin(),
              contentType = ContentType.Text.Html,
              status = HttpStatusCode.NotFound,
          )
    }
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
