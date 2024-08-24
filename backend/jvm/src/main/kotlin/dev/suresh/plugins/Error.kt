package dev.suresh.plugins

import dev.suresh.http.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*

fun Application.errorRoutes() {

  install(StatusPages) {
    status(HttpStatusCode.Unauthorized) { call, status ->
      when {
        call.isApi ->
            call.respondError(
                HttpStatusCode.Unauthorized, "Authorization is required to access this resource")
      }
    }

    exception<Throwable> { call, cause ->
      val status =
          when (cause) {
            is BadRequestException -> HttpStatusCode.BadRequest
            else -> HttpStatusCode.InternalServerError
          }

      call.application.log.error(status.description, cause)
      call.respondError(status, cause.message ?: "Unknown error", cause)
    }

    unhandled {
      it.respondError(
          HttpStatusCode.NotFound, "The requested URL ${it.request.path()} was not found")
    }
  }
}

fun userError(message: Any): Nothing = throw BadRequestException(message.toString())

suspend fun ApplicationCall.respondError(
    status: HttpStatusCode,
    message: String,
    cause: Throwable? = null
) =
    respond(
        status = status,
        message =
            ErrorStatus(
                code = status.value,
                message = message,
                details = if (debug) cause?.stackTraceToString() else cause?.message))
