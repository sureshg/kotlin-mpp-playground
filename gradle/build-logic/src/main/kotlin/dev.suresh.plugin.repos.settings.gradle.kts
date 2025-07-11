@file:Suppress("UnstableApiUsage")

import com.gradle.develocity.agent.gradle.scan.PublishedBuildScan
import com.javiersc.semver.settings.gradle.plugin.SemverSettingsExtension
import common.*
import kotlinx.kover.gradle.aggregation.settings.dsl.KoverSettingsExtension
import org.gradle.api.JavaVersion.VERSION_21
import org.gradle.kotlin.dsl.*
import org.gradle.toolchains.foojay.FoojayToolchainResolver
import org.tomlj.Toml

val versionCatalog by lazy {
  // A hack to read the version catalog from settings
  runCatching {
        Toml.parse(settingsDir.resolve("gradle/libs.versions.toml").readText()).getTable("versions")
      }
      .getOrNull()
}

pluginManagement {
  require(JavaVersion.current().isCompatibleWith(VERSION_21)) {
    "This build requires Gradle to be run with at least Java $VERSION_21"
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
    googleAndroid()
    mavenSnapshot()
  }
}

// Apply the plugins to all projects
plugins {
  id("com.gradle.develocity")
  id("org.gradle.toolchains.foojay-resolver")
  id("org.jetbrains.kotlinx.kover.aggregation")
  id("com.javiersc.semver")
  // id("dev.suresh.plugin.include")
}

// Centralizing repositories declaration
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
    googleAndroid()
    mavenSnapshot()
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

configure<SemverSettingsExtension> {
  // val ktVersion = versionCatalog?.getString("kotlin").orEmpty()
  // mapVersion { it.copy(metadata = ktVersion).toString() }
}

configure<KoverSettingsExtension> {
  enableCoverage()
  reports {
    excludedClasses.addAll("*.generated.*", "dev.suresh.example.*")
    verify { warningInsteadOfFailure = true }
  }
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
      ivy(versionCatalog?.getString("repo-nodejs").orEmpty()) {
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
  val mvnSnapshot = gradleBooleanProp("maven.snapshot.repo.enabled").get()
  if (mvnSnapshot) {
    logger.lifecycle("❖ Maven Snapshot is enabled!")
    maven(url = versionCatalog?.getString("repo-mvn-snapshot").orEmpty()) {
      name = "Central Portal Snapshots"
      mavenContent { snapshotsOnly() }
      // content { includeModule("dev.suresh", "app") }
    }
  }
}

/** Add build scan details to the GitHub Job summary report! */
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
