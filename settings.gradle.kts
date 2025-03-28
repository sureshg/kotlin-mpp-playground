pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
  includeBuild("gradle/build-logic")
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

if (isComposeEnabled) {
  include(":compose:cmp")
  // include(":compose:cli")
  // include(":compose:html")
}

if (isSpringBootEnabled) {
  include(":backend:boot")
}

// includeBuild("misc/build") {
//    dependencySubstitution {
//        substitute(module("dev.suresh:misc-build")).using(project(":"))
//    }
// }

val Settings.isNativeTargetEnabled: Boolean
  get() = gradleBooleanProp("kotlin.target.native.enabled").get()

val Settings.isComposeEnabled: Boolean
  get() = gradleBooleanProp("composeBuild").get()

val Settings.isSpringBootEnabled: Boolean
  get() = gradleBooleanProp("springBoot").get()

val Settings.isWinTargetEnabled: Boolean
  get() = gradleBooleanProp("kotlin.target.win.enabled").get()

fun Settings.gradleBooleanProp(name: String): Provider<Boolean> =
    providers.gradleProperty(name).map(String::toBoolean).orElse(false)
