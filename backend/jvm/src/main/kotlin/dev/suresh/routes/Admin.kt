package dev.suresh.routes

import BuildConfig
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Level.INFO
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.util.ContextInitializer
import io.ktor.http.ContentType
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger.ROOT_LOGGER_NAME
import org.slf4j.LoggerFactory

fun Routing.adminRoutes() {
  swaggerUI(path = "docs", swaggerFile = "openapi/documentation.yaml") {
    version = BuildConfig.swaggerUi
    customStyle(BuildConfig.swaggerStyle)
  }

  get("/") { call.respondRedirect("/docs") }

  authenticate("admin") {
    route("/loglevel") {
      get("/{logger}") {
        val loggerName = call.parameters["logger"] ?: ROOT_LOGGER_NAME
        val loggerCtx = LoggerFactory.getILoggerFactory() as LoggerContext
        when (val logger = loggerCtx.getLogger(loggerName)) {
          null -> call.respondText("Logger '$loggerName' not found", status = NotFound)
          else -> call.respondText("Logger '$loggerName' has level: ${logger.level}")
        }
      }

      post("/{logger}/{level}") {
        val loggerName = call.parameters["logger"] ?: ROOT_LOGGER_NAME
        val levelName = call.parameters["level"]?.uppercase() ?: INFO.levelStr
        try {
          val loggerCtx = LoggerFactory.getILoggerFactory() as LoggerContext
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
}
