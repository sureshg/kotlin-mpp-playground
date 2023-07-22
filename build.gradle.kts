plugins {
  plugins.common
  plugins.misc
  // kotlin("multiplatform") apply false
}

dependencies { project.subprojects.forEach { kover(it) } }

tasks.dokkaHtmlMultiModule { moduleName = "Kotlin Multiplatform Playground!" }
