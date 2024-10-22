import common.jvmTarget

plugins {
  dev.suresh.plugin.kotlin.mpp
  dev.suresh.plugin.publishing
}

description = "Kotlin KSP Processor"

kotlin {
  jvmTarget(project)

  sourceSets {
    commonMain { dependencies { implementation(projects.shared) } }

    jvmMain {
      dependencies {
        implementation(libs.kotlin.ksp.api)
        // implementation("com.squareup:kotlinpoet-ksp")
      }

      kotlin.srcDir("src/main/kotlin")
      resources.srcDir("src/main/resources")
    }
  }
}
