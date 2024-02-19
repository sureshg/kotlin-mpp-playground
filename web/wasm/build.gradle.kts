@file:Suppress("UnstableApiUsage")

plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

description = "WasmJS Web application"

val sharedResources by configurations.creating

tasks {
  val copySharedResources by
      registering(Copy::class) {
        from(sharedResources)
        into(wasmJsProcessResources.get().destinationDir)
      }
  wasmJsProcessResources { dependsOn(copySharedResources) }
}

dependencies {
  commonMainImplementation(projects.shared)
  // sharedResources(project( path = projects.shared.dependencyProject.path, configuration =
  // "sharedWasmResources"))
}

// Expose browser dist as configuration to be consumed by other projects
val wasmApp by configurations.consumable("wasmApp")

artifacts { add(wasmApp.name, tasks.wasmJsBrowserDistribution) }
