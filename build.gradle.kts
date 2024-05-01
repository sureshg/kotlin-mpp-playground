import common.mavenCentralPassword
import common.mavenCentralUsername

plugins {
  plugins.common
  plugins.misc
  com.gradleup.nmcp
  // kotlin("multiplatform") apply false
  // alias(libs.plugins.kotlin.compose.compiler) apply false
  // alias(libs.plugins.jetbrains.compose) apply false
}

description = "Kotlin Multiplatform Playground!"

nmcp {
  publishAggregation {
    project(projects.shared.dependencyProject.path)
    project(projects.web.js.dependencyProject.path)
    project(projects.web.wasm.dependencyProject.path)
    project(projects.depMgmt.bom.dependencyProject.path)
    project(projects.depMgmt.catalog.dependencyProject.path)
    project(projects.meta.ksp.processor.dependencyProject.path)
    project(projects.meta.compiler.plugin.dependencyProject.path)
    project(projects.backend.jvm.dependencyProject.path)
    project(projects.backend.data.dependencyProject.path)
    project(projects.backend.profiling.dependencyProject.path)
    project(projects.backend.security.dependencyProject.path)
    username = mavenCentralUsername
    password = mavenCentralPassword
    publicationType = "USER_MANAGED"
  }
}
