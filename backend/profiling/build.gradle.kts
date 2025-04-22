import common.jvmTarget

plugins {
  dev.suresh.plugin.kotlin.mpp
  dev.suresh.plugin.publishing
}

description = "JVM Profiling and Monitoring!"

kotlin {
  jvmTarget(project)

  sourceSets {
    commonMain { dependencies { implementation(projects.shared) } }

    jvmMain {
      dependencies {
        implementation(libs.jmc.common)
        implementation(libs.jmc.jfr)
        implementation(libs.ap.jfr.converter)
        implementation(libs.bytesize)
        // implementation(libs.ap.loader.all)
      }
      kotlin.srcDir("src/main/kotlin")
      resources.srcDir("src/main/resources")
    }

    jvmTest {
      kotlin.srcDir("src/test/kotlin")
      resources.srcDir("src/test/resources")
    }
  }
}
