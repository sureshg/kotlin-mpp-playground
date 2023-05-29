package settings

import org.gradle.kotlin.dsl.*
import org.gradle.toolchains.foojay.FoojayToolchainResolver

pluginManagement {
  require(JavaVersion.current().isJava11Compatible) {
    "This build requires Gradle to be run with at least Java 11"
  }
}

// Apply the plugins to all projects
plugins {
  id("org.gradle.toolchains.foojay-resolver")
  id("com.javiersc.semver")
}

// Centralizing repositories declaration
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    mavenCentral()

    // Fix for https://youtrack.jetbrains.com/issue/KT-56300
    ivy("https://nodejs.org/dist/") {
      name = "Node.js Distributions"
      patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
      metadataSources { artifact() }
      content { includeModule("org.nodejs", "node") }
    }

    ivy("https://github.com/yarnpkg/yarn/releases/download/") {
      name = "Yarn Distributions"
      patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
      metadataSources { artifact() }
      content { includeModule("com.yarnpkg", "yarn") }
    }
  }
  repositoriesMode = RepositoriesMode.PREFER_SETTINGS
}

@Suppress("UnstableApiUsage")
toolchainManagement {
  jvm {
    javaRepositories {
      repository("foojay") { resolverClass = FoojayToolchainResolver::class.java }
    }
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
