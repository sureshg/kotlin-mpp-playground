@file:Suppress("UnstableApiUsage")

import common.*

plugins {
  id("dev.suresh.plugin.kotlin.mpp")
  id("dev.suresh.plugin.publishing")
}

description = "Kotlin JS/Wasm Web application"

kotlin {
  jsTarget(project)
  wasmJsTarget(project)

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared)
      implementation(libs.kotlin.cryptography.optimal)
      implementation(libs.kotlinx.html)
    }

    jsMain.dependencies {
      implementation(npm("highlight.js", libs.versions.npm.highlightjs.get()))
      implementation(npm("@xterm/xterm", libs.versions.npm.xtermjs.get()))
    }
  }
}

val sharedJsRes by configurations.creating
val sharedWasmRes by configurations.creating

dependencies {
  sharedJsRes(project(path = projects.shared.path, configuration = "sharedJsResources"))
  sharedWasmRes(project(path = projects.shared.path, configuration = "sharedWasmResources"))
}

tasks {
  val jsResources = named<ProcessResources>("jsProcessResources")
  val wasmJsResources = named<ProcessResources>("wasmJsProcessResources")

  val copySharedJsResources by
      registering(Sync::class) {
        from(sharedJsRes)
        into(jsResources.map { it.destinationDir })
        includeEmptyDirs = false
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }
  val copySharedWasmResources by
      registering(Sync::class) {
        from(sharedWasmRes)
        into(wasmJsResources.map { it.destinationDir })
        includeEmptyDirs = false
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }

  jsResources { mustRunAfter(copySharedJsResources) }
  wasmJsResources { mustRunAfter(copySharedWasmResources) }
}

artifacts {
  val jsApp by configurations.consumable("jsApp")
  add(jsApp.name, tasks.named("jsBrowserDistribution"))
  val wasmApp by configurations.consumable("wasmApp")
  add(wasmApp.name, tasks.named("wasmJsBrowserDistribution"))
}
