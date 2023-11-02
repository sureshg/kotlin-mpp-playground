@file:JvmName("CommonPlatform")

package dev.suresh

import BuildConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.jvm.JvmName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json

expect val platform: Platform

interface Platform {

  val name: String

  val tzShortId
    get() = TimeZone.currentSystemDefault().id

  val vtDispatcher
    get() = Dispatchers.Default

  val buildConfig
    get() = BuildConfig

  val appInfo
    get() =
        mapOf(
            "name" to buildConfig.name,
            "description" to buildConfig.description,
            "version" to buildConfig.version,
        )

  val osInfo: Map<String, String?>
    get() = emptyMap()

  val info
    get() =
        with(buildConfig) {
          mapOf(
              "app" to appInfo,
              "build" to
                  mapOf(
                      "time" to "$buildTimeLocal $tzShortId",
                      "version" to version,
                      "os" to buildOS,
                      "user" to buildUser,
                      "host" to buildHost,
                      "jdk" to buildJdkVersion,
                      "gradle" to gradle,
                      "jdk-vendor" to buildJdkVendor,
                      "java-release-version" to java,
                      "kotlin-jvm-target" to kotlinJvmtarget),
              "runtime" to
                  mapOf(
                      "java" to sysProp("java.runtime.version", "n/a"),
                      "kotlin" to KotlinVersion.CURRENT.toString(),
                      "platform" to "Kotlin ${this@Platform.name}",
                  ),
              "git" to
                  mapOf(
                      "commit-hash" to gitHash,
                      "commit-message" to gitMessage,
                      "commit-time" to epochSecToString(gitTimestampEpochSecond.toLong()),
                      "tag" to gitTags),
              "os" to osInfo)
        }

  fun env(key: String, def: String? = null): String? = def

  fun sysProp(key: String, def: String? = null): String? = def

  fun epochSecToString(epochSeconds: Long) =
      "${Instant.fromEpochSeconds(epochSeconds).toLocalDateTime(TimeZone.currentSystemDefault())} $tzShortId"
}

/** Common JSON instance for serde of JSON data. */
val json by lazy {
  Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
    decodeEnumsCaseInsensitive = true
  }
}

val log = KotlinLogging.logger {}

/** Gets the current date and time in UTC timezone. */
val utcDateTimeNow
  get() = Clock.System.now().toLocalDateTime(TimeZone.UTC)

/** Gets the current date and time in the system's default time zone. */
val localDateTimeNow
  get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

/**
 * Runs the given suspend block on a virtual thread, so that we can call blocking I/O APIs from
 * coroutines
 */
suspend inline fun <T> runOnVirtualThread(crossinline block: suspend CoroutineScope.() -> T): T =
    withContext(platform.vtDispatcher) { block() }

/** A coroutine scope that uses [Platform.vtDispatcher] as its dispatcher. */
val virtualThreadScope
  get() = CoroutineScope(platform.vtDispatcher)
