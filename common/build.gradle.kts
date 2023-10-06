@file:Suppress("UnstableApiUsage")

import common.jvmArguments
import common.versionCatalogMapOf

plugins {
  application
  plugins.kotlin.mpp
  plugins.publishing
}

description = "Shared common module for all projects"

application {
  mainClass = libs.versions.app.mainclass
  applicationDefaultJvmArgs += jvmArguments(forAppRun = true)
}

buildConfig { catalogVersions = project.versionCatalogMapOf() }

dependencies {
  commonMainApi(libs.arrow.suspendapp)
  commonMainApi(libs.uri.kmp)
  commonMainApi(libs.ajalt.colormath)
  commonMainApi(libs.benasher44.uuid)
  commonMainApi(libs.intellij.markdown)
  commonMainApi(libs.kotlin.codepoints.deluxe)
  commonMainApi(libs.multiplatform.settings.core)
  commonMainApi(libs.parsus)

  // jvmMainApi(libs.kotlin.reflect)
  // jvmMainApi(libs.logback.classic)
}

// Expose common js resource as configuration to be consumed by other projects
// https://docs.gradle.org/current/userguide/cross_project_publications.html#sec:simple-sharing-artifacts-between-projects
kotlin.sourceSets.jsMain {
  val commonJsResources by configurations.consumable("commonJsResources")
  artifacts { add(commonJsResources.name, tasks.jsProcessResources) }
}

// configurations {
//   // Collects dependencies, constraints to be used by Consumable and Resolvable configurations.
//   val webResources by dependencyScope("webResources")
//   // Acts as the root of a dependency graph
//   val webResourcesRuntimeClasspath by
//       resolvable("webResourcesRuntimeClasspath") { extendsFrom(webResources) }
//   // Models the outgoing variants of a project component.
//   val webResourcesRuntimeElements by
//       consumable("webResourcesRuntimeElements") { extendsFrom(webResources) }
// }
