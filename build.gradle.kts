plugins {
  dev.suresh.plugin.root
  // alias(libs.plugins.kotlin.multiplatform) apply false
  // id(libs.plugins.kotlin.multiplatform.get().pluginId)
}

description = "Kotlin Multiplatform Playground!"

dependencies {
  dokka(projects.shared)
  dokka(projects.meta.ksp.processor)
  dokka(projects.meta.compiler.plugin)
  dokka(projects.backend.jvm)
  dokka(projects.backend.data)
  dokka(projects.backend.profiling)
  dokka(projects.backend.security)
  dokka(projects.web)

  findProject(":backend:native")?.let { dokka(it) }
  findProject(":compose:cmp")?.let { dokka(it) }
  findProject(":compose:html")?.let { dokka(it) }
}
