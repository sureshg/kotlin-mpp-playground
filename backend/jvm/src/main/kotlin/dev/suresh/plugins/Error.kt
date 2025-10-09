package dev.suresh.plugins

import dev.suresh.http.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*

fun Application.errorRoutes() {

  install(StatusPages) {
    status(Unauthorized) { call, status ->
      if (call.isApi) {
        call.respondError(Unauthorized, "Authorization required")
      }
    }

    status(HttpStatusCode.NotFound) { call, _ ->
      call.respondRedirect("/app", permanent = true) // 301
    }

    exception<BadRequestException> { call, cause ->
      call.application.log.error("Serialization error", cause)
      call.respondError(BadRequest, "Invalid request payload", cause.rootCause())
    }

    exception<RequestValidationException> { call, cause ->
      call.respondError(BadRequest, cause.reasons.joinToString())
    }

    exception<ClientRequestException> { call, cause ->
      call.respondError(cause.response.status, cause.response.status.description)
    }

    exception<Throwable> { call, cause ->
      call.application.log.error("Internal Server Error", cause)
      call.respondError(
          InternalServerError,
          "Unable to process request. Please try again",
          cause,
      )
    }

    unhandled {
      it.application.log.error("Unhandled error: ${it.request.path()}")
      it.respondError(NotFound, "Resource not found")
    }
  }
}

fun userError(message: Any): Nothing = throw BadRequestException(message.toString())

suspend fun ApplicationCall.respondError(
    status: HttpStatusCode,
    message: String,
    cause: Throwable? = null,
) =
    respond(
        status = status,
        message =
            ErrorStatus(
                code = status.value,
                message = message,
                details = if (debug) cause?.stackTraceToString() else cause?.message,
            ),
    )

tailrec fun Throwable.rootCause(): Throwable = cause?.rootCause() ?: this
