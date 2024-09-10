@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
  val mvnSnapshot = providers.gradleProperty("enableMavenSnapshot").orNull.toBoolean()
  val mvnSnapshotRepo by lazy {
    file(rootDir)
        .resolveSibling("libs.versions.toml")
        .readLines()
        .first { it.contains("mvn-snapshot-repo") }
        .split("\"")[1]
        .trim()
  }

  repositories {
    mavenCentral()
    gradlePluginPortal()

    if (mvnSnapshot) {
      logger.lifecycle("❖ Maven Snapshot is enabled for build-logic!")
      maven(url = mvnSnapshotRepo) { mavenContent { snapshotsOnly() } }
    }
  }

  versionCatalogs { register("libs") { from(files("../libs.versions.toml")) } }
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "build-logic"
