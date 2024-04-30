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

// tasks.buildConfig { outputs.upToDateWhen { false } }

dependencies {
  commonMainApi(libs.uri.kmp)
  commonMainApi(libs.benasher44.uuid)
  commonMainApi(libs.intellij.markdown)
  commonMainApi(libs.kotlinx.jsonpath)
  commonMainApi(libs.kotlin.codepoints.deluxe)
  commonMainApi(libs.multiplatform.settings.core)

  // commonMainApi(libs.arrow.suspendapp)
  // commonMainApi(libs.parsus)
  // commonMainApi(libs.okio)
  // commonTestApi(libs.okio.fakefilesystem)
  // jvmMainApi(libs.kotlin.reflect)
  // jvmMainApi(libs.logback.classic)
}
