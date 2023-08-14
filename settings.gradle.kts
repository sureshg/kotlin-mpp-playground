pluginManagement { includeBuild("gradle/build-logic") }

plugins { id("settings.repos") }

rootProject.name = "kotlin-mpp-playground"

include(":common")

include(":api-client")

include(":backend")

include(":web")

include(":benchmarks")

include(":compose:web")

include(":compose:desktop")

// includeBuild("misc/build") {
//    dependencySubstitution {
//        substitute(module("dev.suresh:misc-build")).using(project(":"))
//    }
// }

include(":dep-mgmt:bom")

include(":dep-mgmt:catalog")

include(":devtools:ksp:processor")

include(":devtools:compiler:plugin")
