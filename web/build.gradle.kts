@file:Suppress("UnstableApiUsage")

import com.github.ajalt.mordant.rendering.TextColors

plugins {
  plugins.kotlin.mpp
  plugins.publishing
  // alias(libs.plugins.karakum)
  // alias(libs.plugins.seskar)
}

description = "Kotlin JS/Wasm Web application"

val sharedJsRes by configurations.creating
val sharedWasmRes by configurations.creating

dependencies {
  commonMainImplementation(projects.shared)
  jsMainImplementation(npm("highlight.js", libs.versions.npm.highlightjs.get()))
  jsMainImplementation(npm("@xterm/xterm", libs.versions.npm.xtermjs.get()))
  jsMainImplementation(libs.kotlin.cryptography.webcrypto)
  // jsMainImplementation(libs.seskar.core)
  wasmJsMainImplementation(libs.kotlin.cryptography.webcrypto)

  sharedJsRes(
      project(path = projects.shared.dependencyProject.path, configuration = "sharedJsResources"))
  sharedWasmRes(
      project(path = projects.shared.dependencyProject.path, configuration = "sharedWasmResources"))
}

tasks {
  val copySharedJsResources by
      registering(Sync::class) {
        from(sharedJsRes)
        into(jsProcessResources.get().destinationDir)
        includeEmptyDirs = false
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }
  val copySharedWasmResources by
      registering(Sync::class) {
        from(sharedWasmRes)
        into(wasmJsProcessResources.get().destinationDir)
        includeEmptyDirs = false
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }

  jsProcessResources {
    logger.quiet(TextColors.gray("◈ Copying shared JS resources"))
    dependsOn(copySharedJsResources)
  }

  wasmJsProcessResources {
    logger.quiet(TextColors.gray("◈ Copying shared Wasm resources"))
    dependsOn(copySharedWasmResources)
  }
}

artifacts {
  val jsApp by configurations.consumable("jsApp")
  add(jsApp.name, tasks.jsBrowserDistribution)
  val wasmApp by configurations.consumable("wasmApp")
  add(wasmApp.name, tasks.wasmJsBrowserDistribution)
}
