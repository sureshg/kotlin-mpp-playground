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

val sharedJsRes = configurations.create("sharedJsRes")
val sharedWasmRes = configurations.create("sharedWasmRes")

dependencies {
  sharedJsRes(project(path = projects.shared.path, configuration = "sharedJsResources"))
  sharedWasmRes(project(path = projects.shared.path, configuration = "sharedWasmResources"))
}

tasks {
  val jsResources = named<ProcessResources>("jsProcessResources")
  val wasmJsResources = named<ProcessResources>("wasmJsProcessResources")

  val copySharedJsResources =
      register<Sync>("copySharedJsResources") {
        from(sharedJsRes)
        into(jsResources.map { it.destinationDir })
        includeEmptyDirs = false
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }
  val copySharedWasmResources =
      register<Sync>("copySharedWasmResources") {
        from(sharedWasmRes)
        into(wasmJsResources.map { it.destinationDir })
        includeEmptyDirs = false
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }

  jsResources { mustRunAfter(copySharedJsResources) }
  wasmJsResources { mustRunAfter(copySharedWasmResources) }
}

artifacts {
  val jsApp = configurations.consumable("jsApp")
  add(jsApp.name, tasks.named("jsBrowserDistribution"))
  val wasmApp = configurations.consumable("wasmApp")
  add(wasmApp.name, tasks.named("wasmJsBrowserDistribution"))
}
