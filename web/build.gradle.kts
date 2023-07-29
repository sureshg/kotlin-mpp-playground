import common.commonProjectName

plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

description = "Web application"

// To copy common resources to JS/Wasm targets
val commonWebResources by
    configurations.creating {
      isCanBeConsumed = false
      isCanBeResolved = true
    }

tasks {
  val copyCommonJsResources by
      registering(Copy::class) {
        from(commonWebResources)
        into(jsProcessResources.get().destinationDir)
      }

  jsProcessResources { dependsOn(copyCommonJsResources) }
}

dependencies {
  commonWebResources(
      project(path = ":$commonProjectName", configuration = configurations.commonJsResources.name))

  commonMainImplementation(projects.common)
  jsMainImplementation(libs.kotlinx.html)
  jsMainImplementation(npm("highlight.js", libs.versions.npm.highlightjs.get()))
  // jsMainImplementation(npm("kotlin-playground", libs.versions.npm.kotlin.playground.get()))
  // jsMainImplementation(npm("xterm", libs.versions.npm.xtermjs.get()))
  // jsMainImplementation(npm("vega-lite", libs.versions.npm.vega.lite.get()))
}

// kotlin.sourceSets.jsMain.configure {
//  println(implementationConfigurationName)
//  dependencies { implementation(projects.common) }
// }

// List all jsMain dependencies
// configurations.jsMainImplementation.get().allDependencies.forEach { println(it.name) }
