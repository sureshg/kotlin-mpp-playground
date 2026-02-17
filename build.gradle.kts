plugins {
  id("dev.suresh.plugin.root")
  // alias(libs.plugins.kotlin.multiplatform) apply false
  // id(libs.plugins.kotlin.multiplatform.get().pluginId)
}

description = "Kotlin Multiplatform Playground!"

dependencies {
  dokka(project(":shared"))
  dokka(project(":meta:ksp:processor"))
  dokka(project(":meta:compiler:plugin"))
  dokka(project(":backend:jvm"))
  dokka(project(":backend:data"))
  dokka(project(":backend:profiling"))

  findProject(":web")?.let { dokka(it) }
  findProject(":backend:native")?.let { dokka(it) }
  findProject(":compose:cmp")?.let { dokka(it) }
  findProject(":compose:html")?.let { dokka(it) }
}
