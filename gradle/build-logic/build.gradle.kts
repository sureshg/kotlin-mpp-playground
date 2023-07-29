plugins { alias(libs.plugins.benmanes) }

tasks {
  dependencyUpdates { checkConstraints = true }

  register("clean") {
    group = "build"
    description = "Cleans all projects"
    subprojects.mapNotNull { it.tasks.findByName("clean") }.forEach { dependsOn(it) }
    doLast { delete(layout.buildDirectory) }
  }
}
