pluginManagement { includeBuild("gradle/build-logic") }

plugins { id("settings.repos") }

rootProject.name = "kotlin-mpp-playground"

include(":shared")

include(":web:js")

include(":web:wasm")

include(":benchmark")

include(":dep-mgmt:bom")

include(":dep-mgmt:catalog")

include(":meta:ksp:processor")

include(":meta:compiler:plugin")

include(":backend:jvm")

include(":backend:security")

include(":backend:data")

include(":backend:profiling")

val nativeBuild: String? by settings
val composeBuild: String? by settings
val springBoot: String? by settings

if (nativeBuild.toBoolean()) {
  include(":backend:native")
}

if (composeBuild.toBoolean()) {
  include(":compose:desktop")
  // include(":compose:html")
}

if (springBoot.toBoolean()) {
  include(":backend:boot")
}

// includeBuild("misc/build") {
//    dependencySubstitution {
//        substitute(module("dev.suresh:misc-build")).using(project(":"))
//    }
// }
