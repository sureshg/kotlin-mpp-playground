package dev.suresh.routes

import BuildConfig
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Level.INFO
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.util.ContextInitializer
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.ExperimentalKtorApi
import org.slf4j.Logger.ROOT_LOGGER_NAME
import org.slf4j.LoggerFactory

@OptIn(ExperimentalKtorApi::class)
fun Routing.adminRoutes() {
  get("/") { call.respondRedirect("/docs") }

  swaggerUI(path = "/docs") {
    info =
        OpenApiInfo(
            title = BuildConfig.name,
            version = BuildConfig.version,
            description = BuildConfig.description,
        )

    source = OpenApiDocSource.Routing { routingRoot.descendants() }
    deepLinking = true
    servers { server("http://localhost:8080") }

    version = BuildConfig.swaggerUi
    customStyle(BuildConfig.swaggerStyle)
  }

  authenticate("admin", optional = false) {
        route("/loglevel") {
          get("/{logger}") {
                val loggerName = call.parameters["logger"] ?: ROOT_LOGGER_NAME
                val loggerCtx = LoggerFactory.getILoggerFactory() as LoggerContext
                when (val logger = loggerCtx.getLogger(loggerName)) {
                  null -> call.respondText("Logger '$loggerName' not found", status = NotFound)
                  else -> call.respondText("Logger '$loggerName' has level: ${logger.level}")
                }
              }
              .describe {
                parameters {
                  path("logger") {
                    description = "Logger name (e.g., 'dev.suresh')"
                    required = false
                  }
                }
                responses {
                  HttpStatusCode.OK {
                    description = "Current log level of the logger"
                    // schema = jsonSchema<List<Message>>()
                  }
                  HttpStatusCode.NotFound {
                    description = "Logger not found"
                    ContentType.Text.Plain()
                  }
                }

                summary = "Get log level for a specific logger"
                description = "Retrieves the current log level for the specified logger"
              }

          /**
           * Set log level for a logger and its sub-packages.
           *
           * Path parameters:
           * - logger [String] Logger name (e.g., 'dev.suresh', 'io.ktor'). Defaults to root logger.
           * - level [String] Log level to set (TRACE, DEBUG, INFO, WARN, ERROR, OFF). Case
           *   insensitive. Defaults to INFO.
           */
          post("/{logger}/{level}") {
            val loggerName = call.parameters["logger"] ?: ROOT_LOGGER_NAME
            val levelName = call.parameters["level"]?.uppercase() ?: INFO.levelStr
            try {
              val loggerCtx = LoggerFactory.getILoggerFactory() as LoggerContext
              // loggerCtx.isPackagingDataEnabled = true
              val matching =
                  loggerCtx.loggerList
                      .filter { it.name.startsWith(prefix = loggerName, ignoreCase = true) }
                      .onEach { it.level = Level.valueOf(levelName) }

              when (matching.isNotEmpty()) {
                true -> call.respondText("Set $loggerName and sub-packages to $levelName")
                else -> call.respondText("Logger '$loggerName' not found", status = NotFound)
              }
            } catch (_: Exception) {
              call.respondText("Invalid log level: $levelName", status = BadRequest)
            }
          }

          post("/reset") {
            val loggerCtx = LoggerFactory.getILoggerFactory() as LoggerContext
            loggerCtx.reset()
            ContextInitializer(loggerCtx).autoConfig()
            call.respondText("Logback configuration reset!")
          }
        }

        staticResources("/resources", ".") { contentType { ContentType.Text.Plain } }
      }
      .describe { tag("Admin") }
}
