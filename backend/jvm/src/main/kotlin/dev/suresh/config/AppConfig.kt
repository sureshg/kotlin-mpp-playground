package dev.suresh.config

import com.typesafe.config.ConfigFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.config.ApplicationConfig
import kotlin.reflect.full.withNullability
import kotlin.reflect.typeOf
import kotlin.time.Duration
import kotlinx.serialization.hocon.*

/**
 * Initializes the config data classes from the application config. Since HOCON is used for
 * application configuration, `kotlinx.serialization.hocon` is used to deserialize the config. The
 * [AppConfig.init] method should be called before accessing the config values.
 */
data object AppConfig {

  val log = KotlinLogging.logger {}

  private lateinit var appConfig: ApplicationConfig

  private val hocon = Hocon {
    serializersModule = Hocon.serializersModule
    encodeDefaults = true
  }

  /** Initializes application config */
  fun init(config: ApplicationConfig) {
    log.info { "Initializing App configurations..." }
    appConfig = config
  }

  /** Application authn/authz configuration. */
  val auth by lazy {
    val config = ConfigFactory.parseMap(appConfig.config("app.auth").toMap())
    hocon.decodeFromConfig<Map<String, String>>(config)
  }
}

/**
 * Extension function to get and convert config values to their respective type. Nullability is
 * disabled to support java types
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
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
