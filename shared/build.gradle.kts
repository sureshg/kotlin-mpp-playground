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
  applicationDefaultJvmArgs += jvmArguments(appRun = true)
}

buildConfig {
  projectName = rootProject.name
  projectVersion = project.version.toString()
  projectDesc = rootProject.description
  gitCommit = semver.commits.get().first()
  catalogVersions = project.versionCatalogMapOf()
}

kotlin.sourceSets {
  // Expose shared js resource as configuration to be consumed by other projects
  // https://docs.gradle.org/current/userguide/cross_project_publications.html#sec:simple-sharing-artifacts-between-projects
  val sharedJsRes by configurations.consumable("sharedJsResources")
  val sharedWasmRes by configurations.consumable("sharedWasmResources")

  jsMain { artifacts { add(sharedJsRes.name, tasks.jsProcessResources) } }
  // wasmJsMain { artifacts { add(sharedWasmRes.name, tasks.wasmJsProcessResources) } }
}

dependencies {
  commonMainApi(libs.arrow.suspendapp)
  commonMainApi(libs.uri.kmp)
  commonMainApi(libs.ajalt.colormath)
  commonMainApi(libs.benasher44.uuid)
  commonMainApi(libs.kotlin.bignum)
  commonMainApi(libs.intellij.markdown)
  commonMainApi(libs.kotlinx.jsonpath)
  commonMainApi(libs.kotlin.codepoints.deluxe)
  commonMainApi(libs.multiplatform.settings.core)
  commonMainApi(libs.parsus)

  // jvmMainApi(libs.kotlin.reflect)
  // jvmMainApi(libs.logback.classic)
}

// tasks.buildConfig {
//  outputs.upToDateWhen { false }
// }

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
