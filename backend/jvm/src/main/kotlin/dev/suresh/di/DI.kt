package dev.suresh.di

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.property
import io.ktor.server.plugins.di.DI
import io.ktor.server.plugins.di.DefaultConflictPolicy
import io.ktor.server.plugins.di.DependencyConflictPolicy
import io.ktor.server.plugins.di.dependencies
import kotlin.reflect.full.withNullability
import kotlin.reflect.typeOf
import kotlin.time.Duration

suspend fun Application.configureDI() {
  // Override dependencies
  install(DI) {
    conflictPolicy = DependencyConflictPolicy { prev, curr ->
      when (val result = DefaultConflictPolicy.resolve(prev, curr)) {
        is Conflict -> KeepNew
        else -> result
      }
    }
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
