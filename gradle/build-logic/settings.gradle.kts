@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

  versionCatalogs { create("libs") { from(files("../libs.versions.toml")) } }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "build-logic"

include("common-plugins")
