plugins {
  `java-platform`
  plugins.publishing
}

description = "A platform (BOM) used to align all module versions"

dependencies {
  constraints {
    // api(projects.backend)
    rootProject.subprojects.forEach { project -> api(project) }
  }
}
