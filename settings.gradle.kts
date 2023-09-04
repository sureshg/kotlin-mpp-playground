pluginManagement { includeBuild("gradle/build-logic") }

plugins { id("settings.repos") }

rootProject.name = "kotlin-mpp-playground"

listOf(
        "common",
        "backend",
        "client",
        "web",
        "benchmark",
        "compose:web",
        "compose:desktop",
        "dep-mgmt:bom",
        "dep-mgmt:catalog",
        "meta:ksp:processor",
        "meta:compiler:plugin")
    .forEach {
      include(":$it")
    }

// includeBuild("misc/build") {
//    dependencySubstitution {
//        substitute(module("dev.suresh:misc-build")).using(project(":"))
//    }
// }
