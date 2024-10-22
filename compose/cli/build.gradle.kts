import common.jvmRunArgs

plugins {
  application
  com.gradleup.shadow
  dev.suresh.plugin.kotlin.mpp
  dev.suresh.plugin.publishing
  alias(libs.plugins.kotlin.compose.compiler)
  // alias(libs.plugins.detekt)
}

description = "Compose mosaic CLI app!"

kotlin {
  jvmTarget(project)

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared)
      implementation(libs.mosaic.runtime)
    }
  }
}

application {
  mainClass = libs.versions.app.mainclass.get()
  applicationDefaultJvmArgs += project.jvmRunArgs
}
