@file:Suppress("UnstableApiUsage")

package settings

import com.gradle.develocity.agent.gradle.scan.PublishedBuildScan
import common.GithubAction
import common.Repo
import kotlinx.kover.gradle.aggregation.settings.dsl.KoverSettingsExtension
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

  // val kotlinVersion =
  //     file("$rootDir/gradle/libs.versions.toml")
  //         .readLines()
  //         .first { it.contains("kotlin") }
  //         .split("\"")[1]
  //         .trim()

  plugins {
    // val kspVersion: String by settings
    // id("com.google.devtools.ksp") version kspVersion apply false
    // kotlin("multiplatform") version(extra["kotlin.version"] as String) apply false
  }

  repositories {
    mavenCentral()
    gradlePluginPortal()
    googleAndroid()
    kobWeb()
  }
}

// Apply the plugins to all projects
plugins {
  id("com.gradle.develocity")
  id("org.gradle.toolchains.foojay-resolver")
  id("org.jetbrains.kotlinx.kover.aggregation")
  id("com.autonomousapps.build-health")
  id("com.javiersc.semver")
  // Include other pre-compiled settings plugin
  id("settings.include")
}

// Centralizing repositories declaration
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
    googleAndroid()
    kobWeb()
  }

  // Enable back after the KMP Node.js repo fix.
  // repositoriesMode = RepositoriesMode.PREFER_SETTINGS
}

@Suppress("UnstableApiUsage")
toolchainManagement {
  jvm {
    javaRepositories {
      repository("foojay") { resolverClass = FoojayToolchainResolver::class.java }
    }
  }
}

configure<KoverSettingsExtension> {
  enableCoverage()
  reports { excludedClasses.addAll("*.generated.*", "dev.suresh.example.*") }
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"

    capture {
      buildLogging = false
      testLogging = false
    }

    obfuscation {
      ipAddresses { it.map { _ -> "0.0.0.0" } }
      hostname { "*******" }
      username { it.reversed() }
    }

    publishing.onlyIf { GithubAction.isEnabled }
    uploadInBackground = false
    tag("GITHUB_ACTION")
    buildScanPublished { addJobSummary() }
  }
}

fun RepositoryHandler.googleAndroid() {
  google {
    mavenContent {
      includeGroupAndSubgroups("androidx")
      includeGroupAndSubgroups("com.android")
      includeGroupAndSubgroups("com.google")
    }
  }
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

fun RepositoryHandler.mavenSnapshot() {
  maven(url = Repo.SONATYPE_SNAPSHOT) { mavenContent { snapshotsOnly() } }
}

fun RepositoryHandler.kobWeb() {
  maven(url = Repo.KOBWEB) {
    name = "KobWeb Repo"
    content { includeGroupAndSubgroups("com.varabyte") }
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
