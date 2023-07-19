plugins {
  plugins.common
  plugins.misc
  kotlin("multiplatform") apply false
}

// Multi module single coverage report
dependencies { project.subprojects.forEach { kover(it) } }

tasks.dokkaHtmlMultiModule { moduleName = "Kotlin Multiplatform Playground!" }
