@file:Suppress("UnstableApiUsage")

plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

description = "Web application"

// Copy common JS resources to webapp
val webResources by configurations.creating

tasks {
  val copyCommonJsResources by
      registering(Copy::class) {
        from(webResources)
        into(jsProcessResources.get().destinationDir)
      }
  jsProcessResources { dependsOn(copyCommonJsResources) }
}

dependencies {
  commonMainImplementation(projects.common)
  jsMainImplementation(npm("highlight.js", libs.versions.npm.highlightjs.get()))
  webResources(project(":${projects.common.name}", "commonJsResources"))
  // jsMainImplementation(npm("kotlin-playground", libs.versions.npm.kotlin.playground.get()))
  // jsMainImplementation(npm("xterm", libs.versions.npm.xtermjs.get()))
  // jsMainImplementation(npm("vega-lite", libs.versions.npm.vega.lite.get()))
}

// Expose browser dist as configuration to be consumed by other projects
val webApp by configurations.consumable("webApp")

artifacts {
  add(webApp.name, tasks.jsBrowserDistribution)
}

// List all jsMain dependencies
// configurations.jsMainImplementation.get().allDependencies.forEach { println(it.name) }
