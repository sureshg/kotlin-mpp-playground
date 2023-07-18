plugins {
  plugins.common
  plugins.misc
  kotlin("multiplatform") apply false
}

// Multi module single coverage report
// tasks.dokkaHtmlMultiModule { moduleName = project.name }
dependencies { project.subprojects.forEach { kover(it) } }
