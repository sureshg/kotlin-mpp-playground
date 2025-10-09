@file:Suppress("UnstableApiUsage")

import common.*
import org.jetbrains.kotlin.gradle.dsl.abi.*

plugins {
  id("dev.suresh.plugin.kotlin.mpp")
  id("dev.suresh.plugin.publishing")
  // alias(libs.plugins.mappie)
}

description = "Shared common module for all projects"

buildConfig {
  enabled = true
  projectName = rootProject.name
  projectVersion = project.version.toString()
  projectDesc = rootProject.description
  gitCommit = semver.commits.get().first()
  catalogVersions = project.versionCatalogMapOf()
}

kotlin {
  jvmTarget(project)
  jsTarget(project)
  wasmJsTarget(project)
  if (isNativeTargetEnabled) {
    nativeTargets(project)
  }

  @OptIn(ExperimentalAbiValidation::class)
  abiValidation.enabled = true
}

// dependencies {
//   commonMainApi(libs.bundles.ajalt)
//   commonMainApi(libs.evas)
//   commonMainApi(libs.uri.kmp)
//   commonMainApi(libs.kotlin.bignum)
//   commonMainApi(libs.kotlin.bignum.serialization)
//   commonMainApi(libs.intellij.markdown)
//   commonMainApi(libs.kotlin.codepoints.deluxe)
//   commonMainApi(libs.kotlin.cryptography.core)
//   commonMainApi(libs.kotlin.cryptography.random)
//   commonMainApi(libs.multiplatform.settings.coroutines)
//   commonMainApi(libs.bundles.json.extra)
//   commonMainApi(libs.urlencoder)
//   commonMainApi(libs.parsus)
//   jvmMainApi(libs.kotlin.reflect)
//   jvmMainApi(libs.logback.classic)
// }
