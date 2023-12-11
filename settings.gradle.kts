import common.Platform

pluginManagement { includeBuild("gradle/build-logic") }

plugins { id("settings.repos") }

rootProject.name = "kotlin-mpp-playground"

include(":common")

include(":web")

include(":benchmark")
// include(":compose:web")
include(":compose:desktop")

include(":dep-mgmt:bom")

include(":dep-mgmt:catalog")

include(":meta:ksp:processor")

include(":meta:compiler:plugin")

include(":backend:jvm")

if (Platform.isUnix) {
  // include(":backend:native")
}

// includeBuild("misc/build") {
//    dependencySubstitution {
//        substitute(module("dev.suresh:misc-build")).using(project(":"))
//    }
// }
