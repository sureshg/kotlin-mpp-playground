import common.commonProjectName

plugins { plugins.kotlin.mpp }

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

  jsMainImplementation(projects.common)
  jsMainImplementation(libs.kotlinx.html)
  jsMainImplementation(npm("highlight.js", "11.8.0"))
  // jsMainImplementation(npm("kotlin-playground", "1.28.0"))
  // jsMainImplementation(npm("xterm", "5.2.1"))
  // jsMainImplementation(npm("vega-lite", "5.13.0"))
}

// kotlin.sourceSets.jsMain.configure {
//  println(implementationConfigurationName)
//  dependencies { implementation(projects.common) }
// }

// List all jsMain dependencies
// configurations.jsMainImplementation.get().allDependencies.forEach { println(it.name) }
