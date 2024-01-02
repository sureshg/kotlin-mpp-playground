@file:Suppress("UnstableApiUsage")

plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

description = "WasmJS Web application"

val wasmResources by configurations.creating

tasks {
  val copySharedWasmRes by
      registering(Copy::class) {
        from(wasmResources)
        into(wasmJsProcessResources.get().destinationDir)
      }

  wasmJsProcessResources { dependsOn(copySharedWasmRes) }
}

dependencies {
  commonMainImplementation(projects.shared)
  // wasmResources(project(
  //    path = projects.shared.dependencyProject.path,
  //    configuration = "sharedWasmResources"
  // ))
}

// Expose browser dist as configuration to be consumed by other projects
val wasmApp by configurations.consumable("wasmApp")

artifacts { add(wasmApp.name, tasks.wasmJsBrowserDistribution) }
