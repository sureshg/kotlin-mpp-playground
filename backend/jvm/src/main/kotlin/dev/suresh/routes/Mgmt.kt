package dev.suresh.routes

import dev.suresh.Profiling
import dev.suresh.jvmRuntimeInfo
import dev.suresh.plugins.debug
import io.ktor.http.*
import io.ktor.http.ContentDisposition.Companion.Attachment
import io.ktor.http.ContentDisposition.Parameters.FileName
import io.ktor.http.HttpHeaders.ContentDisposition
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.websocket.Frame.*
import java.io.File
import java.lang.ScopedValue.CallableOp
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val DEBUG = ScopedValue.newInstance<Boolean>()

val mutex = Mutex()

val docRoot = Path(System.getProperty("java.io.tmpdir"))

fun Routing.mgmtRoutes() {

  staticFiles(remotePath = "/tmp", dir = docRoot.toFile())

  get("/info") {
    call.respond(
        ScopedValue.where(DEBUG, call.debug).call(CallableOp { jvmRuntimeInfo(DEBUG.get()) }))
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
                |<!DOCTYPE html>
                |<html>
                |<head>
                |    <title>File Browser</title>
                |    <style>
                |        body {
                |            font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
                |            margin: 0;
                |            padding: 0;
                |            background-color: #FFFFFF;
                |            color: #333;
                |        }
                |
                |        .container {
                |            max-width: 900px;
                |            margin: auto;
                |            padding: 1em;
                |        }
                |
                |        h2 {
                |            font-weight: 500;
                |            margin-bottom: 1em;
                |        }
                |
                |        ul {
                |            list-style: none;
                |            padding: 0;
                |            margin: 0;
                |        }
                |
                |        ul li {
                |            display: flex;
                |            align-items: center;
                |            background-color: #F9F9F9;
                |            padding: 0.5em;
                |            margin: 0.2em 0;
                |            border-radius: 3px;
                |            transition: background 0.15s ease-in-out;
                |        }
                |
                |        ul li:hover {
                |            background-color: #E9E9E9;
                |        }
                |
                |        ul li a {
                |            text-decoration: none;
                |            color: #333;
                |        }
                |
                |        ul li i {
                |            margin-right: 0.5em;
                |        }
                |
                |        .fa-folder::before {
                |            content: url("data:image/svg+xml,%3Csvg fill='%23000000' viewBox='0 0 24 24' xmlns='http://www.w3.org/2000/svg' width='24' height='24'%3E%3Cpath d='M19 5.5h-6.28l-0.32 -1a3 3 0 0 0 -2.84 -2H5a3 3 0 0 0 -3 3v13a3 3 0 0 0 3 3h14a3 3 0 0 0 3 -3v-10a3 3 0 0 0 -3 -3Zm1 13a1 1 0 0 1 -1 1H5a1 1 0 0 1 -1 -1v-13a1 1 0 0 1 1 -1h4.56a1 1 0 0 1 0.95 0.68l0.54 1.64a1 1 0 0 0 0.95 0.68h7a1 1 0 0 1 1 1Z'/%3E%3C/svg%3E");
                |        }
                |        .fa-file::before {
                |            content: url("data:image/svg+xml,%3Csvg fill='%23000000' viewBox='0 0 24 24' xmlns='http://www.w3.org/2000/svg' width='24' height='24'%3E%3Cpath d='M20 8.94a1.31 1.31 0 0 0 -0.06 -0.27v-0.09a1.07 1.07 0 0 0 -0.19 -0.28l-6 -6a1.07 1.07 0 0 0 -0.28 -0.19h-0.09L13.06 2H7a3 3 0 0 0 -3 3v14a3 3 0 0 0 3 3h10a3 3 0 0 0 3 -3V8.94Zm-6 -3.53L16.59 8H14ZM18 19a1 1 0 0 1 -1 1H7a1 1 0 0 1 -1 -1V5a1 1 0 0 1 1 -1h5v5a1 1 0 0 0 1 1h5Z'/%3E%3C/svg%3E");
                |        }
                |    </style>
                |</head>
                |<body>
                |<div class="container">
                |    <h2>Directory listing for: ${reqPath}</h2>
                |    <ul>
                |        ${path.toFile().list().orEmpty().joinToString("\n") { file ->
                              val icon = if (File(file).isDirectory) "<i class=\"fa-folder\"></i>" else "<i class=\"fa-file\"></i>"
                              "<li>${icon}<a href=\"/browse/${file}\">${file}</a></li>"
                             }
                         }
                |    </ul>
                |</div>
                |</body>
                |</html>
                |"""
                    .trimMargin(),
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
    when (mutex.isLocked) {
      true -> call.respondText("Profile operation is already running")
      else ->
          mutex.withLock {
            when (call.request.queryParameters.contains("download")) {
              true -> {
                val jfrPath = Profiling.jfrSnapshot()
                call.response.header(
                    ContentDisposition,
                    Attachment.withParameter(FileName, jfrPath.fileName.name).toString())
                call.respondFile(jfrPath.toFile())
                jfrPath.deleteIfExists()
              }
              else ->
                  call.respondText(contentType = ContentType.Text.Html) { Profiling.flameGraph() }
            }
          }
    }
  }

  get("/heapdump") {
    val heapDumpPath = Profiling.heapdump()
    call.response.header(
        ContentDisposition,
        Attachment.withParameter(FileName, heapDumpPath.fileName.name).toString())
    call.respondFile(heapDumpPath.toFile())
    heapDumpPath.deleteIfExists()
  }

  webSocketRaw("/term") {
    val ip = call.request.origin.remoteHost
    application.log.info("Got WebSocket connection from $ip")
    send("Connected to server using WebSocket: $ip")
    send("Type 'hi' to proceed")

    // create concurrent hashset
    val conn = ConcurrentHashMap.newKeySet<Frame>()
    for (frame in incoming) {
      when (frame) {
        is Text -> {
          val text = frame.readText()
          application.log.info("Received $text")
          when (text.lowercase()) {
            "hi" -> send("Hello, $ip!")
            "bye" -> {
              send("Goodbye, $ip. Closing from client!")
              close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
            }
            else -> send("Sorry, I don't understand")
          }
        }
        is Binary -> application.log.info("Binary frame ${frame.data.decodeToString()}")
        is Close -> application.log.info("Connection closed from Server")
        else -> application.log.info("Unknown frame ${frame.frameType}")
      }
    }
  }
}

// val profiler: AsyncProfiler? by lazy {
//  val ap = AsyncProfilerLoader.loadOrNull()
//  ap.start(Events.CPU, 1000)
//  ap
// }
