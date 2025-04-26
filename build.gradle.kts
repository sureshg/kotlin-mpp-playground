import common.*

plugins {
  dev.suresh.plugin.root
  // alias(libs.plugins.kotlin.multiplatform) apply false
  // id(libs.plugins.kotlin.multiplatform.get().pluginId)
}

description = "Kotlin Multiplatform Playground!"

nmcp {
  centralPortal {
    username = mavenCentralUsername
    password = mavenCentralPassword
    publishingType = "AUTOMATIC"
  }
}

dependencies {
  dokka(projects.shared)
  dokka(projects.meta.ksp.processor)
  dokka(projects.meta.compiler.plugin)
  dokka(projects.backend.jvm)
  dokka(projects.backend.data)
  dokka(projects.backend.profiling)
  dokka(projects.backend.security)
  dokka(projects.web)

  nmcpAggregation(projects.shared)
  nmcpAggregation(projects.depMgmt.bom)
  nmcpAggregation(projects.depMgmt.catalog)
  nmcpAggregation(projects.meta.ksp.processor)
  nmcpAggregation(projects.meta.compiler.plugin)
  nmcpAggregation(projects.backend.jvm)
  nmcpAggregation(projects.backend.data)
  nmcpAggregation(projects.backend.profiling)
  nmcpAggregation(projects.backend.security)
  nmcpAggregation(projects.web)

  // Optional modules
  findProject(":backend:native")?.let { nmcpAggregation(it) }
  findProject(":compose:cmp")?.let { nmcpAggregation(it) }
  findProject(":compose:html")?.let { nmcpAggregation(it) }
}
