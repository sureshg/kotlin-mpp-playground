@file:Suppress("UnstableApiUsage")

plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

description = "Kotlin/JS Web application"

val sharedResources by configurations.creating

tasks {
  val copySharedResources by
      registering(Copy::class) {
        from(sharedResources)
        into(jsProcessResources.get().destinationDir)
      }
  jsProcessResources { dependsOn(copySharedResources) }
}

dependencies {
  commonMainImplementation(projects.shared)
  jsMainImplementation(npm("highlight.js", libs.versions.npm.highlightjs.get()))
  // Add shared JS resources to web
  sharedResources(
      project(path = projects.shared.dependencyProject.path, configuration = "sharedJsResources"))

  // jsMainImplementation(npm("kotlin-playground", libs.versions.npm.kotlin.playground.get()))
  // jsMainImplementation(npm("xterm", libs.versions.npm.xtermjs.get()))
  // jsMainImplementation(npm("vega-lite", libs.versions.npm.vega.lite.get()))
}

artifacts {
  val jsApp by configurations.consumable("jsApp")
  add(jsApp.name, tasks.jsBrowserDistribution)
}
