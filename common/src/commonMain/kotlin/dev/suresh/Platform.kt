package dev.suresh

import BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect fun platform(): Platform

interface Platform {

  val name: String

  val utcTimeNow
    get() = Clock.System.now().toLocalDateTime(TimeZone.UTC)

  val tzShortId
    get() = TimeZone.currentSystemDefault().id

  val vtDispatcher
    get() = Dispatchers.Default

  fun env(key: String, def: String? = null): String? = def

  fun sysProp(key: String, def: String? = null): String? = def

  val buildConfig
    get() = BuildConfig

  val osInfo: Map<String, String?>
    get() = emptyMap()

  val info
    get() =
        with(buildConfig) {
          mapOf(
              "app" to
                  mapOf(
                      "name" to name,
                      "description" to description,
                      "version" to version,
                  ),
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
                  mapOf("commit-hash" to gitHash, "commit-message" to gitMessage, "tag" to gitTags),
              "os" to osInfo)
        }
}
