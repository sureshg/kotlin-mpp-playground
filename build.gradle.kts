import common.*

plugins {
  dev.suresh.plugin.root
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
  dokka(project(":backend:security"))
  dokka(project(":web"))
}

nmcp {
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
    // project(":backend:native")
    // project(":compose:cmp")
    // project(":compose:html")

    username = mavenCentralUsername
    password = mavenCentralPassword
    publicationType = "AUTOMATIC"
  }
}
