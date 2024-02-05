package settings

import com.gradle.scan.plugin.PublishedBuildScan
import common.GithubAction
import common.Repo
import org.gradle.api.JavaVersion.VERSION_17
import org.gradle.kotlin.dsl.*
import org.gradle.toolchains.foojay.FoojayToolchainResolver

pluginManagement {
  require(JavaVersion.current().isCompatibleWith(VERSION_17)) {
    "This build requires Gradle to be run with at least Java $VERSION_17"
  }

  resolutionStrategy {
    eachPlugin {
      when (requested.id.id) {
        "app.cash.licensee" ->
            useModule("app.cash.licensee:licensee-gradle-plugin:${requested.version}")
      }
    }
  }

  plugins {
    // val kspVersion: String by settings
    // id("com.google.devtools.ksp") version kspVersion apply false
    // kotlin("multiplatform") version(extra["kotlin.version"] as String) apply false
  }

  repositories {
    mavenCentral()
    gradlePluginPortal()
    composeMultiplatformDev()
  }
}

// Apply the plugins to all projects
plugins {
  // Gradle build scan
  id("com.gradle.enterprise")
  // Toolchains resolver using the Foojay Disco API.
  id("org.gradle.toolchains.foojay-resolver")
  // Use semver on all projects
  id("com.javiersc.semver")
  // Include other pre-compiled settings plugin
  id("settings.include")
}

// Centralizing repositories declaration
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
    nodeJS()
    yarn()
    composeMultiplatformDev()
    // sonatypeSnapshots()
  }
  repositoriesMode = RepositoriesMode.PREFER_SETTINGS
}

fun RepositoryHandler.nodeJS() {
  exclusiveContent {
    forRepository {
      ivy(Repo.NODEJS) {
        name = "Node Distributions at $url"
        patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
        metadataSources { artifact() }
        content { includeModule("org.nodejs", "node") }
      }
    }
    filter { includeGroup("org.nodejs") }
  }
}

fun RepositoryHandler.yarn() {
  exclusiveContent {
    forRepository {
      ivy(Repo.YARN) {
        name = "Yarn Distributions at $url"
        patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
        metadataSources { artifact() }
        content { includeModule("com.yarnpkg", "yarn") }
      }
    }
    filter { includeGroup("com.yarnpkg") }
  }
}

fun RepositoryHandler.sonatypeSnapshots() {
  maven(url = Repo.SONATYPE_SNAPSHOT) { mavenContent { snapshotsOnly() } }
  maven(url = Repo.SONATYPE_SNAPSHOT_01) { mavenContent { snapshotsOnly() } }
}

/**
 * [Compose-Multiplatform-Compiler](https://github.com/JetBrains/compose-multiplatform/blob/master/VERSIONING.md#using-the-compose-multiplatform-compiler)
 */
@Suppress("UnstableApiUsage")
fun RepositoryHandler.composeMultiplatformDev() {
  maven(url = Repo.COMPOSE_MULTIPLATFORM_DEV) {
    name = "Jetbrains Compose Multiplatform Development Repository"
    content { includeGroupAndSubgroups("org.jetbrains.compose") }
  }
  maven(url = Repo.KOBWEB) {
    name = "KobWeb Repo"
    content { includeGroupAndSubgroups("com.varabyte") }
  }
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    capture { isTaskInputFiles = false }

    obfuscation { ipAddresses { addresses -> addresses.map { _ -> "0.0.0.0" } } }

    if (GithubAction.isEnabled) {
      publishAlways()
      isUploadInBackground = false
      tag("GITHUB_ACTION")
      buildScanPublished { addJobSummary() }
    }
  }
}

@Suppress("UnstableApiUsage")
toolchainManagement {
  jvm {
    javaRepositories {
      repository("foojay") { resolverClass = FoojayToolchainResolver::class.java }
    }
  }
}
/** Add build scan details to GitHub Job summary report! */
fun PublishedBuildScan.addJobSummary() =
    with(GithubAction) {
      setOutput("build_scan_uri", buildScanUri)
      addJobSummary(
          """
          | ##### 🚀 Gradle BuildScan [URL](${buildScanUri.toASCIIString()})
          """
              .trimMargin())
    }

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
