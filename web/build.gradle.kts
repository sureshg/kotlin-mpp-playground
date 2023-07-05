import common.commonProjectName

plugins { plugins.kotlin.mpp }

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
  jsMainImplementation(projects.common)
  commonWebResources(
      project(path = ":$commonProjectName", configuration = configurations.commonJsResources.name))
}

// kotlin.sourceSets.jsMain.configure {
//  println(implementationConfigurationName)
//  dependencies { implementation(projects.common) }
// }

// List all jsMain dependencies
// configurations.jsMainImplementation.get().allDependencies.forEach { println(it.name) }
