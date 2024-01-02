@file:Suppress("UnstableApiUsage")

plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

description = "Kotlin/JS Web application"

val jsResources by configurations.creating

tasks {
  val copySharedJsRes by
      registering(Copy::class) {
        from(jsResources)
        into(jsProcessResources.get().destinationDir)
      }
  jsProcessResources { dependsOn(copySharedJsRes) }
}

dependencies {
  commonMainImplementation(projects.shared)
  jsMainImplementation(npm("highlight.js", libs.versions.npm.highlightjs.get()))
  // Add shared JS resources to web
  jsResources(
      project(path = projects.shared.dependencyProject.path, configuration = "sharedJsResources"))

  // jsMainImplementation(npm("kotlin-playground", libs.versions.npm.kotlin.playground.get()))
  // jsMainImplementation(npm("xterm", libs.versions.npm.xtermjs.get()))
  // jsMainImplementation(npm("vega-lite", libs.versions.npm.vega.lite.get()))
}

artifacts {
  val jsApp by configurations.consumable("jsApp")
  add(jsApp.name, tasks.jsBrowserDistribution)
}
