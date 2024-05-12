@file:Suppress("UnstableApiUsage")

plugins {
  plugins.kotlin.mpp
  plugins.publishing
  // alias(libs.plugins.karakum)
  // alias(libs.plugins.seskar)
}

description = "Kotlin/JS Web application"

val sharedRes by configurations.creating

tasks {
  val copySharedResources by
      registering(Copy::class) {
        from(sharedRes)
        into(jsProcessResources.get().destinationDir)
      }
  jsProcessResources { dependsOn(copySharedResources) }
}

dependencies {
  commonMainImplementation(projects.shared)
  jsMainImplementation(npm("highlight.js", libs.versions.npm.highlightjs.get()))
  jsMainImplementation(npm("@xterm/xterm", libs.versions.npm.xtermjs.get()))
  // Add shared JS resources to web
  sharedRes(
      project(path = projects.shared.dependencyProject.path, configuration = "sharedJsResources"))

  // jsMainImplementation(libs.seskar.core)
  // jsMainImplementation(npm("kotlin-playground", libs.versions.npm.kotlin.playground.get()))
  // jsMainImplementation(npm("vega-lite", libs.versions.npm.vega.lite.get()))
}

artifacts {
  val jsApp by configurations.consumable("jsApp")
  add(jsApp.name, tasks.jsBrowserDistribution)
}
