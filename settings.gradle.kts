pluginManagement {
  includeBuild("gradle/build-logic")
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

plugins { id("settings.repos") }

rootProject.name = "kotlin-mpp-playground"

include(":common")

include(":api-client")

include(":backend")

include(":web")

include(":compose-web")

include("desktop")
