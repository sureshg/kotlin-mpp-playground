import common.*

pluginManagement {
  val buildPluginVer =
      settingsDir
          .resolve("gradle/libs.versions.toml")
          .readLines()
          .first { it.contains("bc-plugins") }
          .split("\"")[1]
          .trim()

  resolutionStrategy {
    eachPlugin {
      if (requested.id.id.startsWith("dev.suresh.plugin")) {
        useVersion(buildPluginVer)
      }
    }
  }

  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("dev.suresh.plugin.repos") }

rootProject.name = "kotlin-mpp-playground"

include(":shared")

include(":dep-mgmt:bom")

include(":dep-mgmt:catalog")

include(":backend:jvm")

include(":backend:security")

include(":backend:data")

include(":backend:profiling")

include(":backend:agent:jfr")

include(":backend:agent:otel")

include(":web")

include(":benchmark")

include(":meta:ksp:processor")

include(":meta:compiler:plugin")

if (isNativeTargetEnabled) {
  include(":backend:native")
}

if (isComposeModuleEnabled) {
  include(":compose:cmp")
  // include(":compose:cli")
  // include(":compose:html")
}

if (isBootModuleEnabled) {
  include(":backend:boot")
}

//// includeBuild("misc/build") {
////    dependencySubstitution {
////        substitute(module("dev.suresh:misc-build")).using(project(":"))
////    }
//// }
