@file:Suppress("UnstableApiUsage")

plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

description = "Web application"

val jsResources by configurations.creating

tasks {
  val copyCommonJsResources by
      registering(Copy::class) {
        from(jsResources)
        into(jsProcessResources.get().destinationDir)
      }
  jsProcessResources { dependsOn(copyCommonJsResources) }
}

dependencies {
  commonMainImplementation(projects.shared)
  jsMainImplementation(npm("highlight.js", libs.versions.npm.highlightjs.get()))
  // Add commonJS resources to web
  jsResources(project(":${projects.shared.name}", "commonJsResources"))

  // jsMainImplementation(npm("kotlin-playground", libs.versions.npm.kotlin.playground.get()))
  // jsMainImplementation(npm("xterm", libs.versions.npm.xtermjs.get()))
  // jsMainImplementation(npm("vega-lite", libs.versions.npm.vega.lite.get()))
}

// Expose browser dist as configuration to be consumed by other projects
val webapp by configurations.consumable("webapp")

artifacts {
  add(webapp.name, tasks.jsBrowserDistribution)
}

// List all jsMain dependencies
// configurations.jsMainImplementation.get().allDependencies.forEach { println(it.name) }
