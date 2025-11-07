package dev.suresh.di

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import kotlin.reflect.full.withNullability
import kotlin.reflect.typeOf
import kotlin.time.Duration

suspend fun Application.configureDI() {
  install(DI) {
    conflictPolicy = DefaultConflictPolicy
    // conflictPolicy = OverridePrevious
  }

  log.info("Initializing config dependencies.")
  val app = this
  dependencies {
    provide { app.property<Auth>("app.auth") }
    // provide(MediaApiClient::class)
    // provide { MediaApiClient(resolve()) }
    // provide(::MediaApiClient)
    // provide<()->Type> { {Factory(resolve(), ...)}}
  }

  log.info("Auth config: ${dependencies.resolve<Auth>()}")
}

/**
 * Extension function to get and convert config values to their respective type. Nullability is
 * disabled to support java types
 */
inline fun <reified T> ApplicationConfig.prop(prop: String) =
    when (typeOf<T>().withNullability(false)) {
      typeOf<String>() -> property(prop).getString()
      typeOf<List<String>>() -> property(prop).getList()
      typeOf<Boolean>() -> property(prop).getString().toBoolean()
      typeOf<Int>() -> property(prop).getString().toInt()
      typeOf<Long>() -> property(prop).getString().toLong()
      typeOf<Double>() -> property(prop).getString().toDouble()
      typeOf<Duration>() -> Duration.parse(property(prop).getString().lowercase())
      else -> throw IllegalArgumentException("Unsupported type: ${typeOf<T>()}")
    }
        as T
