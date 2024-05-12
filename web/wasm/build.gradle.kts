@file:Suppress("UnstableApiUsage")

plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

description = "WasmJS Web application"

val sharedRes by configurations.creating

tasks {
  val copySharedResources by
      registering(Copy::class) {
        from(sharedRes)
        into(wasmJsProcessResources.get().destinationDir)
      }
  wasmJsProcessResources { dependsOn(copySharedResources) }
}

dependencies {
  commonMainImplementation(projects.shared)
  // sharedRes(project(projects.shared.dependencyProject.path,"sharedWasmResources"))
}

artifacts {
  // Expose browser dist as configuration to be consumed by other projects
  val wasmApp by configurations.consumable("wasmApp")
  add(wasmApp.name, tasks.wasmJsBrowserDistribution)
}
