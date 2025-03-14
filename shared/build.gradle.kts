@file:Suppress("UnstableApiUsage")

import common.versionCatalogMapOf

plugins {
  dev.suresh.plugin.kotlin.mpp
  dev.suresh.plugin.publishing
  `binary-compatibility-validator`
  com.jakewharton.`kmp-missing-targets`
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

kotlinMissingTargets {}

dependencies {
  commonMainApi(libs.evas)
  commonMainApi(libs.uri.kmp)
  commonMainApi(libs.intellij.markdown)
  commonMainApi(libs.kotlin.codepoints.deluxe)
  commonMainApi(libs.multiplatform.settings.coroutines)

  // commonMainApi(libs.mappie.api)
  // commonMainApi(libs.bundles.json.extra)
  // commonMainApi(libs.urlencoder)
  // commonMainApi(libs.arrow.suspendapp)
  // commonMainApi(libs.parsus)
  // jvmMainApi(libs.kotlin.reflect)
  // jvmMainApi(libs.logback.classic)
}

// tasks.buildConfig { outputs.upToDateWhen { false } }
