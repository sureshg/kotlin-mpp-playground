@file:Suppress("UnstableApiUsage")

import common.versionCatalogMapOf

plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

description = "Shared common module for all projects"

buildConfig {
  projectName = rootProject.name
  projectVersion = project.version.toString()
  projectDesc = rootProject.description
  gitCommit = semver.commits.get().first()
  catalogVersions = project.versionCatalogMapOf()
}

dependencies {
  commonMainApi(libs.uri.kmp)
  commonMainApi(libs.benasher44.uuid)
  commonMainApi(libs.intellij.markdown)
  commonMainApi(libs.kotlin.codepoints.deluxe)
  commonMainApi(libs.multiplatform.settings.core)

  // commonMainApi(libs.urlencoder)
  // commonMainApi(libs.arrow.suspendapp)
  // commonMainApi(libs.parsus)
  // jvmMainApi(libs.kotlin.reflect)
  // jvmMainApi(libs.logback.classic)
}

// tasks.buildConfig { outputs.upToDateWhen { false } }
