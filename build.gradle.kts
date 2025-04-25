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
  dokka(project(":shared"))
  dokka(project(":meta:ksp:processor"))
  dokka(project(":meta:compiler:plugin"))
  dokka(project(":backend:jvm"))
  dokka(project(":backend:data"))
  dokka(project(":backend:profiling"))
  dokka(project(":backend:security"))
  dokka(project(":web"))

  //  nmcpAggregation(project(":shared"))
  //  nmcpAggregation(project(":dep-mgmt:bom"))
  //  nmcpAggregation(project(":dep-mgmt:catalog"))
  //  nmcpAggregation(project(":meta:ksp:processor"))
  //  nmcpAggregation(project(":meta:compiler:plugin"))
  //  nmcpAggregation(project(":backend:jvm"))
  //  nmcpAggregation(project(":backend:data"))
  //  nmcpAggregation(project(":backend:profiling"))
  //  nmcpAggregation(project(":backend:security"))
  //  nmcpAggregation(project(":web"))
  //  nmcpAggregation(// project(":backend:native")
}
