@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    mavenSnapshot()
  }

  versionCatalogs { register("libs") { from(files("../libs.versions.toml")) } }
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "build-logic"

fun RepositoryHandler.mavenSnapshot() {
  val mvnSnapshot = providers.gradleProperty("enableMavenSnapshot").orNull.toBoolean()
  if (mvnSnapshot) {
    val mvnSnapshotRepo =
        file(rootDir)
            .resolveSibling("libs.versions.toml")
            .readLines()
            .first { it.contains("repo-mvn-snapshot") }
            .split("\"")[1]
            .trim()

    logger.lifecycle("❖ Maven Snapshot is enabled for build-logic!")
    maven(url = mvnSnapshotRepo) { mavenContent { snapshotsOnly() } }
  }
}
