plugins {
  `java-platform`
  dev.suresh.plugin.publishing
}

description = "A platform (BOM) used to align all module versions"

dependencies {
  constraints {
    rootProject.subprojects.filter { it.childProjects.isEmpty() }.forEach { api(it) }
    // api(projects.backend)
  }
}
