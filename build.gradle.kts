import common.*

plugins {
  dev.suresh.plugin.root
  // alias(libs.plugins.kotlin.multiplatform) apply false
  // id(libs.plugins.kotlin.multiplatform.get().pluginId)
}

description = "Kotlin Multiplatform Playground!"

nmcp {
  val nativeBuild: String? by project
  val composeBuild: String? by project

  publishAggregation {
    project(":shared")
    project(":dep-mgmt:bom")
    project(":dep-mgmt:catalog")
    project(":meta:ksp:processor")
    project(":meta:compiler:plugin")
    project(":backend:jvm")
    project(":backend:data")
    project(":backend:profiling")
    project(":backend:security")
    project(":web")
    if (nativeBuild.toBoolean()) {
      project(":backend:native")
    }
    if (composeBuild.toBoolean()) {
      project(":compose:cmp")
      // project(":compose:html")
    }

    username = mavenCentralUsername
    password = mavenCentralPassword
    publicationType = "AUTOMATIC"
  }
}
