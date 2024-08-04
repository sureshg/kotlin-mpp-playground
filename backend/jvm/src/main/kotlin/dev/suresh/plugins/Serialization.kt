package dev.suresh.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*

fun Application.configureSerialization() {
  install(ContentNegotiation) { json(dev.suresh.http.json) }

  install(Resources)
}
