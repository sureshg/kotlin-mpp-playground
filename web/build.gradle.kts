@file:Suppress("UnstableApiUsage")

plugins {
  dev.suresh.plugin.kotlin.mpp
  dev.suresh.plugin.publishing
}

description = "Kotlin JS/Wasm Web application"

val sharedJsRes by configurations.creating
val sharedWasmRes by configurations.creating

dependencies {
  commonMainImplementation(projects.shared)
  commonMainImplementation(libs.kotlin.cryptography.optimal)
  jsMainImplementation(npm("highlight.js", libs.versions.npm.highlightjs.get()))
  jsMainImplementation(npm("@xterm/xterm", libs.versions.npm.xtermjs.get()))

  sharedJsRes(project(path = projects.shared.path, configuration = "sharedJsResources"))
  sharedWasmRes(project(path = projects.shared.path, configuration = "sharedWasmResources"))
}

tasks {
  val copySharedJsResources by
      registering(Sync::class) {
        from(sharedJsRes)
        into(jsProcessResources.map { it.destinationDir })
        includeEmptyDirs = false
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }
  val copySharedWasmResources by
      registering(Sync::class) {
        from(sharedWasmRes)
        into(wasmJsProcessResources.map { it.destinationDir })
        includeEmptyDirs = false
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }

  jsProcessResources { mustRunAfter(copySharedJsResources) }
  wasmJsProcessResources { mustRunAfter(copySharedWasmResources) }
}

artifacts {
  val jsApp by configurations.consumable("jsApp")
  add(jsApp.name, tasks.jsBrowserDistribution)
  val wasmApp by configurations.consumable("wasmApp")
  add(wasmApp.name, tasks.wasmJsBrowserDistribution)
}
