@file:JvmName("CommonPlatform")

package dev.suresh

import BuildConfig
import BuildConfig.Host
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.jvm.JvmName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect val platform: Platform

interface Platform {

  val name: String

  val tzShortId
    get() = TimeZone.currentSystemDefault().id

  val virtualDispatcher: CoroutineDispatcher?
    get() = null

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
                      "os" to Host.os,
                      "user" to Host.user,
                      "host" to Host.name,
                      "cpu-cores" to Host.cpuCores.toString(),
                      "memory" to "${Host.memory/(1_000 * 1_000L)} MB",
                      "jdk" to Host.jdkVersion,
                      "gradle" to gradle,
                      "jdk-vendor" to Host.jdkVendor,
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

val log = KotlinLogging.logger {}
/** Gets the current date and time in UTC timezone. */
val utcDateTimeNow
  get() = Clock.System.now().toLocalDateTime(TimeZone.UTC)

/** Gets the current date and time in the system's default time zone. */
val localDateTimeNow
  get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

// Expect classes are not stable
// expect class Platform {
//    val name: String
// }
