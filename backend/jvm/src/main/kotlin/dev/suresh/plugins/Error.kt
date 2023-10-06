package dev.suresh.plugins

import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*

fun Application.errorRoutes() {
  install(StatusPages) {
    status(Unauthorized) { call, _ -> call.respond(Unauthorized, "Unauthorized") }

    exception<Throwable> { call, cause ->
      val status =
          when (cause) {
            is BadRequestException -> BadRequest
            else -> InternalServerError
          }

      call.application.log.error(status.description, cause)
      call.respond(status, "${cause.message}")
    }

    unhandled { it.respond(NotFound, "The requested URL ${it.request.path()} was not found") }
  }
}

fun userError(message: Any): Nothing = throw BadRequestException(message.toString())
