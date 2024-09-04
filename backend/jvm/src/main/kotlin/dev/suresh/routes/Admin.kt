package dev.suresh.routes

import BuildConfig
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.util.ContextInitializer
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Routing.adminRoutes() {
  swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {
    version = BuildConfig.swaggerUi
    customStyle(BuildConfig.swaggerStyle)
  }

  get("/") { call.respondRedirect("/swagger") }

  route("/loglevel") {
    get("/{logger}") {
      val loggerName = call.parameters["logger"] ?: Logger.ROOT_LOGGER_NAME
      val loggerCtx = LoggerFactory.getILoggerFactory() as LoggerContext
      val logger = loggerCtx.getLogger(loggerName)
      when (logger) {
        null -> call.respondText("Logger '$loggerName' not found", status = HttpStatusCode.NotFound)
        else -> call.respondText("Logger '$loggerName' has level: ${logger.level}")
      }
    }

    post("/{logger}/{level}") {
      val loggerName = call.parameters["logger"] ?: Logger.ROOT_LOGGER_NAME
      val levelName = call.parameters["level"]?.uppercase() ?: Level.INFO.levelStr
      try {
        var loggerFound = false
        val loggerCtx = LoggerFactory.getILoggerFactory() as LoggerContext
        for (logger in loggerCtx.loggerList) {
          if (logger.name.startsWith(prefix = loggerName, ignoreCase = true)) {
            logger.level = Level.valueOf(levelName)
            loggerFound = true
          }
        }

        when (loggerFound) {
          true ->
              call.respondText(
                  "Log level for '$loggerName' and it's sub packages set to $levelName")
          else ->
              call.respondText("Logger '$loggerName' not found", status = HttpStatusCode.NotFound)
        }
      } catch (_: Exception) {
        call.respondText("Invalid log level: $levelName", status = HttpStatusCode.BadRequest)
      }
    }

    post("/reset") {
      val loggerCtx = LoggerFactory.getILoggerFactory() as LoggerContext
      loggerCtx.reset()
      ContextInitializer(loggerCtx).autoConfig()
      call.respondText("Logback configuration reset!")
    }
  }
}
