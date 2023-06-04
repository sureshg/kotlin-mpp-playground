plugins { plugins.kotlin.mpp }

// kotlin.sourceSets.jsMain.configure {
//  println(implementationConfigurationName)
//  dependencies { implementation(projects.common) }
// }
// configurations.jsMainImplementation.get().allDependencies.forEach { println(it.name) }

dependencies { jsMainImplementation(projects.common) }
