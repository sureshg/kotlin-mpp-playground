@file:Suppress("UnstableApiUsage")

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
  wasmJsMainImplementation(libs.kotlin.cryptography.webcrypto)
  // jsMainImplementation(libs.seskar.core)

  sharedJsRes(project(path = ":shared", configuration = "sharedJsResources"))
  sharedWasmRes(project(path = ":shared", configuration = "sharedWasmResources"))
}

tasks {
  val copySharedJsResources by
      registering(Sync::class) {
        group = "Build"
        from(sharedJsRes)
        into(jsProcessResources.get().destinationDir)
        includeEmptyDirs = false
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }
  jsProcessResources { mustRunAfter(copySharedJsResources) }

  val copySharedWasmResources by
      registering(Sync::class) {
        group = "Build"
        from(sharedWasmRes)
        into(wasmJsProcessResources.get().destinationDir)
        includeEmptyDirs = false
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }
  wasmJsProcessResources { mustRunAfter(copySharedWasmResources) }
}

artifacts {
  val jsApp by configurations.consumable("jsApp")
  add(jsApp.name, tasks.jsBrowserDistribution)
  val wasmApp by configurations.consumable("wasmApp")
  add(wasmApp.name, tasks.wasmJsBrowserDistribution)
}
