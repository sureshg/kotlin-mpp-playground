plugins { id("plugins.kotlinMPP") }

dependencies {
  jsMainImplementation(projects.common)
}

// kotlin.sourceSets.jsMain.configure {
//  println(implementationConfigurationName)
//  dependencies { implementation(projects.common) }
// }
// configurations.jsMainImplementation.get().allDependencies.forEach { println(it.name) }
