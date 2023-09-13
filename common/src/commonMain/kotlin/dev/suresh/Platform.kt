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

  val javaRuntimeVersion: String
    get() = "n/a"

  val vtDispatcher
    get() = Dispatchers.Default

  val buildInfo
    get() =
        BuildConfig.run {
          mapOf(
              "Platform" to "Kotlin $name",
              "Build Time" to "$buildTimeLocal $tzShortId",
              "Build Version" to version,
              "Build OS" to buildOS,
              "Build User" to buildUser,
              "Build Host" to buildHost,
              "Build JDK" to buildJdkVersion,
              "Java Runtime Version" to javaRuntimeVersion,
              "Kotlin Runtime Version" to KotlinVersion.CURRENT.toString(),
              "Java Release version" to java,
              "Kotlin JVM Target" to kotlinJvmtarget,
              "Gradle Version" to gradle,
              "Git Hash" to gitHash,
              "Git Message" to gitMessage,
              "Git Tag" to gitTags)
        }
}
